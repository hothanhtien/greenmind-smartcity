import { create } from 'zustand';

export interface OceanModel {
  id: string;
  ocean: string;
  behavior: string;
  context: {
    population: {
      age_range: string;
      gender: string[];
      locations: string[];
      urban: boolean;
    };
    setting: string;
    event: string;
  };
  keywords: string;
}

interface OceanModelState {
  selectedOcean: string;
  selectedBehavior: string;
  context: {
    population: {
      age_from: string;
      age_to: string;
      gender: string[];
      locations: string[];
      urban: boolean;
    };
    setting: string;
    event: string;
  };
  keywords: string;
  generatedKeywords: string[];
  isGenerating: boolean;
  models: OceanModel[];
  setSelectedOcean: (ocean: string) => void;
  setSelectedBehavior: (behavior: string) => void;
  setContext: (context: Partial<OceanModelState['context']>) => void;
  setPopulation: (population: Partial<OceanModelState['context']['population']>) => void;
  setKeywords: (keywords: string) => void;
  setGeneratedKeywords: (keywords: string[]) => void;
  setIsGenerating: (loading: boolean) => void;
  addModel: (model: OceanModel) => void;
  reset: () => void;
}

const initialContext = {
  population: {
    age_from: '18',
    age_to: '30',
    gender: [] as string[],
    locations: [] as string[],
    urban: false,
  },
  setting: '',
  event: '',
};

export const useOceanModelStore = create<OceanModelState>((set) => ({
  selectedOcean: '',
  selectedBehavior: '',
  context: initialContext,
  keywords: '',
  generatedKeywords: [],
  isGenerating: false,
  models: [],
  setSelectedOcean: (ocean) => set({ selectedOcean: ocean }),
  setSelectedBehavior: (behavior) => set({ selectedBehavior: behavior }),
  setContext: (context) =>
    set((state) => ({
      context: { ...state.context, ...context },
    })),
  setPopulation: (population) =>
    set((state) => ({
      context: {
        ...state.context,
        population: { ...state.context.population, ...population },
      },
    })),
  setKeywords: (keywords) => set({ keywords }),
  setGeneratedKeywords: (keywords) => set({ generatedKeywords: keywords }),
  setIsGenerating: (loading) => set({ isGenerating: loading }),
  addModel: (model) => set((state) => ({ models: [...state.models, model] })),
  reset: () =>
    set({
      selectedOcean: '',
      selectedBehavior: '',
      context: initialContext,
      keywords: '',
      generatedKeywords: [],
      isGenerating: false,
    }),
}));
