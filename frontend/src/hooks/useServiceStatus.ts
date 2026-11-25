import { useCallback, useEffect, useRef, useState } from 'react'
import { ApiError, requestJson } from '../api/client'
import type { Language, ServiceStatusResponse } from '../types/service'

const STATUS_PATH = '/v1/status'
const REQUEST_TIMEOUT_MS = 5_000
const POLL_INTERVAL_MS = 15_000
const WAKE_UP_WINDOW_MS = 60_000
const MAX_BACKOFF_MS = 15_000

export type ServiceView = 'warming' | 'operational' | 'maintenance' | 'unavailable'

export interface ServiceStatusState {
  view: ServiceView
  status: ServiceStatusResponse | null
  lastCheckedAt: string | null
  retry: () => void
}

export function useServiceStatus(_language: Language): ServiceStatusState {
  const [view, setView] = useState<ServiceView>('warming')
  const [status, setStatus] = useState<ServiceStatusResponse | null>(null)
  const [lastCheckedAt, setLastCheckedAt] = useState<string | null>(null)
  const [retryVersion, setRetryVersion] = useState(0)
  const startedAt = useRef(Date.now())
  const retry = useCallback(() => setRetryVersion((current) => current + 1), [])

  useEffect(() => {
    let active = true
    let timer = 0
    let failures = 0
    let controller: AbortController | null = null

    const poll = async () => {
      controller = new AbortController()
      try {
        const result = await requestJson<ServiceStatusResponse>(STATUS_PATH, { signal: controller.signal }, REQUEST_TIMEOUT_MS)
        if (!active) return
        failures = 0
        setStatus(result)
        setLastCheckedAt(result.checkedAt)
        if (result.status === 'MAINTENANCE') {
          setView('maintenance')
        } else if (result.components.database === 'DOWN' || result.status === 'DEGRADED') {
          setView(Date.now() - startedAt.current < WAKE_UP_WINDOW_MS ? 'warming' : 'unavailable')
        } else {
          setView('operational')
        }
        timer = window.setTimeout(poll, POLL_INTERVAL_MS)
      } catch (error) {
        if (!active) return
        failures += 1
        setLastCheckedAt(new Date().toISOString())
        const explicitMaintenance = error instanceof ApiError && error.body.code === 'MAINTENANCE'
        if (explicitMaintenance) {
          setStatus({
            status: 'MAINTENANCE',
            components: { api: 'UP', database: 'UP' },
            message: error.body.message,
            estimatedBackAt: null,
            checkedAt: error.body.timestamp,
          })
          setLastCheckedAt(error.body.timestamp)
          setView('maintenance')
        } else {
          setView(Date.now() - startedAt.current < WAKE_UP_WINDOW_MS ? 'warming' : 'unavailable')
        }
        const backoff = Math.min(1_000 * 2 ** Math.min(failures, 4), MAX_BACKOFF_MS)
        timer = window.setTimeout(poll, backoff)
      }
    }

    void poll()
    return () => {
      active = false
      window.clearTimeout(timer)
      controller?.abort()
    }
  }, [retryVersion])

  return { view, status, lastCheckedAt, retry }
}
