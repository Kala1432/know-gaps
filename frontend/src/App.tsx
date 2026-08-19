import React, { useEffect, useState } from 'react';
import { Navbar } from './components/Navbar';
import { DashboardPage } from './pages/DashboardPage';
import { ConceptExplorerPage } from './pages/ConceptExplorerPage';
import { AssessmentPage } from './pages/AssessmentPage';
import { ProgressPage } from './pages/ProgressPage';
import { RecommendationPage } from './pages/RecommendationPage';
import { Student } from './types/api';
import { api } from './api/client';
import { LoadingSpinner } from './components/LoadingSpinner';
import { ErrorState } from './components/ErrorState';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [students, setStudents] = useState<Student[]>([]);
  const [currentStudent, setCurrentStudent] = useState<Student | null>(null);

  const [targetConceptId, setTargetConceptId] = useState<string | undefined>(undefined);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadInitialStudents = async () => {
    setLoading(true);
    setError(null);
    try {
      let studentList = await api.getStudents().catch(() => []);
      if (studentList.length === 0) {
        // Create default student if none exist
        const defaultStudent = await api.createStudent('Alice', 'alice@example.com');
        studentList = [defaultStudent];
      }
      setStudents(studentList);
      setCurrentStudent(studentList[0]);
    } catch (err: any) {
      setError(err.message || 'Failed to initialize student profile.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInitialStudents();
  }, []);

  const handleNavigateTab = (tab: string, conceptId?: string) => {
    setActiveTab(tab);
    setTargetConceptId(conceptId);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-900 text-slate-100 flex items-center justify-center">
        <LoadingSpinner message="Connecting to Knowledge Gap Platform Backend..." />
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-slate-900 text-slate-100 flex items-center justify-center p-4">
        <ErrorState message={error} onRetry={loadInitialStudents} />
      </div>
    );
  }

  if (!currentStudent) {
    return null;
  }

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 flex flex-col font-sans">
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        students={students}
        currentStudent={currentStudent}
        onSelectStudent={setCurrentStudent}
      />

      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {activeTab === 'dashboard' && (
          <DashboardPage student={currentStudent} onNavigateTab={handleNavigateTab} />
        )}
        {activeTab === 'explorer' && (
          <ConceptExplorerPage student={currentStudent} />
        )}
        {activeTab === 'assessment' && (
          <AssessmentPage student={currentStudent} targetConceptId={targetConceptId} />
        )}
        {activeTab === 'progress' && (
          <ProgressPage student={currentStudent} />
        )}
        {activeTab === 'recommendation' && (
          <RecommendationPage student={currentStudent} onNavigateTab={handleNavigateTab} />
        )}
      </main>
    </div>
  );
};
export default App;
