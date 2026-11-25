import { useState } from 'react'
import { Button } from '@heroui/react'
import { MaintenanceScreen, WakingScreen } from './components/MaintenanceScreen'
import { useServiceStatus } from './hooks/useServiceStatus'
import type { Language } from './types/service'

export default function App() {
  const [language, setLanguage] = useState<Language>('en')
  const service = useServiceStatus(language)

  if (service.view === 'warming') return <WakingScreen language={language} />
  if (service.view === 'maintenance' || service.view === 'unavailable') {
    return (
      <>
        <div className="maintenance-language">
          <Button variant="ghost" size="md" onPress={() => setLanguage(language === 'en' ? 'th' : 'en')} aria-label="Change language">
            {language === 'en' ? 'ไทย' : 'English'}
          </Button>
        </div>
        <MaintenanceScreen
          language={language}
          message={service.status?.message}
          estimatedBackAt={service.status?.estimatedBackAt}
          lastCheckedAt={service.lastCheckedAt}
          onRetry={service.retry}
        />
      </>
    )
  }
  return <main className="maintenance-shell"><div className="maintenance-card"><p>THE ASTER HOTEL</p><h1>Welcome</h1><p>Self check-in is ready.</p></div></main>
}
