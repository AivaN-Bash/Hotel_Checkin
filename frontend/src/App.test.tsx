import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import App from './App'

afterEach(() => vi.unstubAllGlobals())

describe('kiosk availability screens', () => {
  it('shows backend maintenance details and returns to welcome after recovery', async () => {
    const response = (status: string, message: string) => new Response(JSON.stringify({
      status,
      components: { api: 'UP', database: 'UP' },
      message,
      estimatedBackAt: null,
      checkedAt: '2026-11-25T00:00:00Z',
    }), { status: 200, headers: { 'Content-Type': 'application/json' } })
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(response('MAINTENANCE', 'We are making a small improvement.'))
      .mockResolvedValueOnce(response('OPERATIONAL', 'Ready'))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    render(<App />)
    expect(await screen.findByRole('heading', { name: 'We’ll be back shortly.' })).toBeInTheDocument()
    expect(screen.getByText('We are making a small improvement.')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Try again' }))
    await waitFor(() => expect(screen.getByRole('heading', { name: 'Welcome' })).toBeInTheDocument())
  })
})
