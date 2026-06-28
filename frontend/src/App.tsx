import { Route, Routes } from 'react-router-dom'
import { TopBar } from '@/components/TopBar'
import { DashboardPage } from '@/pages/DashboardPage'
import { RbacDemoPage } from '@/pages/RbacDemoPage'
import { RolesPage } from '@/pages/RolesPage'

export default function App() {
  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 dark:bg-slate-950 dark:text-slate-200">
      <TopBar />
      <main className="mx-auto max-w-5xl px-5 py-6">
        <Routes>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/rbac" element={<RbacDemoPage />} />
          <Route path="/roles" element={<RolesPage />} />
        </Routes>
      </main>
    </div>
  )
}
