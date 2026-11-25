import { act, renderHook, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { useServiceStatus } from './useServiceStatus'

const operational = { status: 'OPERATIONAL', components: { api: 'UP', database: 'UP' }, message: 'Ready', estimatedBackAt: null, checkedAt: '2026-11-25T00:00:00Z' }
const maintenance = { status: 'MAINTENANCE', components: { api: 'UP', database: 'UP' }, message: 'Scheduled work', estimatedBackAt: null, checkedAt: '2026-11-25T00:00:00Z' }

afterEach(() => { vi.useRealTimers(); vi.unstubAllGlobals() })

describe('useServiceStatus', () => {
  it('shows maintenance immediately when the API declares maintenance', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(jsonResponse(maintenance)))
    const { result } = renderHook(() => useServiceStatus('en'))
    await waitFor(() => expect(result.current.view).toBe('maintenance'))
    expect(result.current.status?.message).toBe('Scheduled work')
  })

  it('recovers from maintenance automatically when status becomes operational', async () => {
    vi.useFakeTimers()
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(jsonResponse(maintenance))
      .mockResolvedValueOnce(jsonResponse(operational))
    vi.stubGlobal('fetch', fetchMock)
    const { result } = renderHook(() => useServiceStatus('en'))
    await act(async () => { await Promise.resolve(); await Promise.resolve() })
    expect(result.current.view).toBe('maintenance')
    await act(async () => { await vi.advanceTimersByTimeAsync(15_000) })
    expect(result.current.view).toBe('operational')
  })

  it('keeps the waking message during an outage, then switches to maintenance after the wake window', async () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2026-11-25T00:00:00Z'))
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response('{}', { status: 503 })))
    const { result } = renderHook(() => useServiceStatus('en'))
    await act(async () => { await Promise.resolve(); await Promise.resolve() })
    expect(result.current.view).toBe('warming')
    await act(async () => {
      vi.setSystemTime(new Date(Date.now() + 65_000))
      result.current.retry()
      await Promise.resolve()
      await Promise.resolve()
      await Promise.resolve()
    })
    expect(result.current.view).toBe('unavailable')
  })
})

function jsonResponse(value: unknown): Response {
  return new Response(JSON.stringify(value), { status: 200, headers: { 'Content-Type': 'application/json' } })
}
