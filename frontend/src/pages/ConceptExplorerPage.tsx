import React, { useEffect, useState } from 'react';
import { Compass, GitBranch, ArrowRight, CheckCircle } from 'lucide-react';
import { Subject, Topic, Concept, PrerequisiteChain, ConceptPerformance, Student } from '../types/api';
import { api } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorState } from '../components/ErrorState';

interface ConceptExplorerPageProps {
  student: Student;
}

export const ConceptExplorerPage: React.FC<ConceptExplorerPageProps> = ({ student }) => {
  const [subjects, setSubjects] = useState<Subject[]>([]);
  const [selectedSubjectId, setSelectedSubjectId] = useState<string | null>(null);

  const [topics, setTopics] = useState<Topic[]>([]);
  const [selectedTopicId, setSelectedTopicId] = useState<string | null>(null);

  const [concepts, setConcepts] = useState<Concept[]>([]);
  const [selectedConcept, setSelectedConcept] = useState<Concept | null>(null);

  const [graph, setGraph] = useState<PrerequisiteChain | null>(null);
  const [perfMap, setPerfMap] = useState<Record<string, ConceptPerformance>>({});

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadCurriculum();
  }, []);

  const loadCurriculum = async () => {
    setLoading(true);
    setError(null);
    try {
      const subs = await api.getSubjects();
      setSubjects(subs);
      if (subs.length > 0) {
        setSelectedSubjectId(subs[0].id);
      }

      // Load overall student performance map
      const perfs = await api.getStudentPerformance(student.id).catch(() => []);
      const map: Record<string, ConceptPerformance> = {};
      perfs.forEach((p) => {
        map[p.conceptId] = p;
      });
      setPerfMap(map);
    } catch (err: any) {
      setError(err.message || 'Failed to load curriculum.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (selectedSubjectId) {
      api.getTopics(selectedSubjectId).then((topList) => {
        setTopics(topList);
        if (topList.length > 0) {
          setSelectedTopicId(topList[0].id);
        } else {
          setConcepts([]);
          setSelectedConcept(null);
        }
      });
    }
  }, [selectedSubjectId]);

  useEffect(() => {
    if (selectedTopicId) {
      api.getConcepts(selectedTopicId).then((conList) => {
        setConcepts(conList);
        if (conList.length > 0) {
          handleSelectConcept(conList[0]);
        } else {
          setSelectedConcept(null);
          setGraph(null);
        }
      });
    }
  }, [selectedTopicId]);

  const handleSelectConcept = async (concept: Concept) => {
    setSelectedConcept(concept);
    try {
      const graphData = await api.getConceptGraph(concept.id);
      setGraph(graphData);
    } catch {
      setGraph(null);
    }
  };

  if (loading) return <LoadingSpinner message="Building concept graph hierarchy..." />;
  if (error) return <ErrorState message={error} onRetry={loadCurriculum} />;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-slate-800/60 p-6 rounded-2xl border border-slate-700/60 flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <Compass className="w-5 h-5 text-indigo-400" /> Concept Explorer & Graph Hierarchy
          </h2>
          <p className="text-sm text-slate-400 mt-1">
            Navigate subject architecture, prerequisite chains, and concept mastery metrics.
          </p>
        </div>
      </div>

      {/* Subject & Topic Selectors */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Subject Select */}
        <div className="bg-slate-800/60 p-4 rounded-xl border border-slate-700">
          <label className="text-xs font-semibold text-slate-400 block mb-2">Subject</label>
          <select
            value={selectedSubjectId || ''}
            onChange={(e) => setSelectedSubjectId(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 text-slate-200 rounded-lg p-2.5 text-sm focus:outline-none focus:border-indigo-500"
          >
            {subjects.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </div>

        {/* Topic Select */}
        <div className="bg-slate-800/60 p-4 rounded-xl border border-slate-700">
          <label className="text-xs font-semibold text-slate-400 block mb-2">Topic</label>
          <select
            value={selectedTopicId || ''}
            onChange={(e) => setSelectedTopicId(e.target.value)}
            className="w-full bg-slate-900 border border-slate-700 text-slate-200 rounded-lg p-2.5 text-sm focus:outline-none focus:border-indigo-500"
          >
            {topics.map((t) => (
              <option key={t.id} value={t.id}>
                {t.name}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Concepts & Prerequisite Details */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Concept List */}
        <div className="bg-slate-800/60 p-5 rounded-2xl border border-slate-700/60 space-y-3">
          <h3 className="text-sm font-semibold text-slate-200 mb-3">Concepts in Topic</h3>
          {concepts.length === 0 ? (
            <p className="text-xs text-slate-400">No concepts found for this topic.</p>
          ) : (
            concepts.map((c) => {
              const isSelected = selectedConcept?.id === c.id;
              const perf = perfMap[c.id];
              return (
                <div
                  key={c.id}
                  onClick={() => handleSelectConcept(c)}
                  className={`p-3.5 rounded-xl border transition cursor-pointer flex items-center justify-between ${
                    isSelected
                      ? 'bg-indigo-600/20 border-indigo-500/50 shadow-md'
                      : 'bg-slate-900/50 border-slate-800 hover:border-slate-700'
                  }`}
                >
                  <div>
                    <span className="font-semibold text-sm text-slate-200 block">{c.name}</span>
                    <span className="text-xs text-slate-400 line-clamp-1">{c.description}</span>
                  </div>
                  {perf && <StatusBadge type="mastery" value={perf.masteryScore} />}
                </div>
              );
            })
          )}
        </div>

        {/* Selected Concept Graph Inspector */}
        <div className="lg:col-span-2 bg-slate-800/60 p-6 rounded-2xl border border-slate-700/60 space-y-6">
          {selectedConcept ? (
            <>
              {/* Concept Overview */}
              <div className="border-b border-slate-700 pb-4">
                <div className="flex items-center justify-between">
                  <h3 className="text-xl font-bold text-slate-100">{selectedConcept.name}</h3>
                  {perfMap[selectedConcept.id] && (
                    <StatusBadge type="mastery" value={perfMap[selectedConcept.id].masteryScore} />
                  )}
                </div>
                <p className="text-sm text-slate-300 mt-2">{selectedConcept.description}</p>
              </div>

              {/* Prerequisite Path Visualizer */}
              <div className="space-y-4">
                <h4 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
                  <GitBranch className="w-4 h-4 text-indigo-400" /> Prerequisite Dependency Graph
                </h4>

                {graph ? (
                  <div className="bg-slate-900/80 p-5 rounded-xl border border-slate-800 space-y-4">
                    {/* Prerequisite Ancestors */}
                    <div>
                      <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-2">
                        Required Prerequisite Ancestors
                      </span>
                      {graph.prerequisitePath.length === 0 ? (
                        <span className="text-xs text-emerald-400 flex items-center gap-1.5">
                          <CheckCircle className="w-4 h-4" /> Foundation Concept (No Prerequisites Required)
                        </span>
                      ) : (
                        <div className="flex flex-wrap items-center gap-2">
                          {graph.prerequisitePath.map((p, idx) => (
                            <React.Fragment key={p.id}>
                              <span className="px-3 py-1.5 rounded-lg bg-indigo-950 text-indigo-300 border border-indigo-800 text-xs font-semibold">
                                {p.name}
                              </span>
                              {idx < graph.prerequisitePath.length - 1 && (
                                <ArrowRight className="w-3.5 h-3.5 text-slate-500" />
                              )}
                            </React.Fragment>
                          ))}
                          <ArrowRight className="w-4 h-4 text-indigo-400 font-bold" />
                          <span className="px-3 py-1.5 rounded-lg bg-indigo-600 text-white text-xs font-bold shadow-lg shadow-indigo-500/20">
                            {selectedConcept.name}
                          </span>
                        </div>
                      )}
                    </div>

                    {/* Downstream Unlocked Concepts */}
                    <div className="pt-3 border-t border-slate-800">
                      <span className="text-xs font-semibold text-slate-400 uppercase tracking-wider block mb-2">
                        Downstream Concepts Unlocked
                      </span>
                      {graph.downstreamConcepts.length === 0 ? (
                        <span className="text-xs text-slate-500">No downstream dependencies registered.</span>
                      ) : (
                        <div className="flex flex-wrap gap-2">
                          {graph.downstreamConcepts.map((d) => (
                            <span key={d.id} className="px-3 py-1.5 rounded-lg bg-slate-800 text-slate-300 border border-slate-700 text-xs font-medium">
                              {d.name}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                ) : (
                  <p className="text-xs text-slate-400">Loading prerequisite graph...</p>
                )}
              </div>
            </>
          ) : (
            <p className="text-sm text-slate-400">Select a concept to inspect its prerequisite graph.</p>
          )}
        </div>
      </div>
    </div>
  );
};
