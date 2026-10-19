import { create } from "zustand"

export interface Node {
  id: string
  name: string
  type: "trait" | "behavior" | "demographic" | "keyword" | "scenario"
  data?: any
  children?: Node[]
}

export interface Question {
  id: string
  text: string
}

export interface Scenario {
  id: string
  name: string
  description?: string
  trait?: string
  behavior?: string
  demographic: { ageRange: [number, number]; location: string }
  percentage: number
  questions: string[]
  usersAssigned: string[]
  status: "draft" | "sent"
  keywords?: string[]
}

export interface User {
  id: string
  name: string
  age: number
  location: string
  status?: "pending" | "completed"
  assignedScenarios?: string[]
}

interface ScenarioStore {
  tree: Node[]
  scenarios: Scenario[]
  users: User[]
  questions: Question[]
  addNode: (parentId: string | null, node: Node) => void
  removeNode: (nodeId: string) => void
  updateNode: (nodeId: string, updates: Partial<Node>) => void
  generateScenario: (
    minAge: number,
    maxAge: number,
    location: string,
    percentage: number,
    description?: string,
    trait?: string,
    behavior?: string,
  ) => void
  selectQuestions: (scenarioId: string, questionIds: string[]) => void
  simulateDistribution: (scenarioId: string) => void
  deleteScenario: (scenarioId: string) => void
  updateUserStatus: (userId: string, status: "completed") => void
  setTree: (tree: Node[]) => void
  exportTreeAsJSON: () => string
  exportScenariosAsJSON: () => string
  importTree: (jsonString: string) => boolean
}

export const useScenarioStore = create<ScenarioStore>((set, get) => ({
  tree: [],
  scenarios: [],
  users: [],
  questions: [],

  setTree: (tree) => set({ tree }),

  addNode: (parentId, node) => {
    set((state) => {
      const newTree = [...state.tree]
      if (!parentId) {
        newTree.push(node)
      } else {
        const addToParent = (nodes: Node[]): Node[] => {
          return nodes.map((n) => {
            if (n.id === parentId) {
              return { ...n, children: [...(n.children || []), node] }
            }
            if (n.children) {
              return { ...n, children: addToParent(n.children) }
            }
            return n
          })
        }
        return { tree: addToParent(newTree) }
      }
      return { tree: newTree }
    })
  },

  removeNode: (nodeId) => {
    set((state) => {
      const removeFromTree = (nodes: Node[]): Node[] => {
        return nodes
          .filter((n) => n.id !== nodeId)
          .map((n) => ({
            ...n,
            children: n.children ? removeFromTree(n.children) : undefined,
          }))
      }
      return { tree: removeFromTree(state.tree) }
    })
  },

  updateNode: (nodeId, updates) => {
    set((state) => {
      const updateInTree = (nodes: Node[]): Node[] => {
        return nodes.map((n) => {
          if (n.id === nodeId) {
            return { ...n, ...updates }
          }
          if (n.children) {
            return { ...n, children: updateInTree(n.children) }
          }
          return n
        })
      }
      return { tree: updateInTree(state.tree) }
    })
  },

  generateScenario: (minAge, maxAge, location, percentage, description, trait, behavior) => {
    const id = `scenario-${Date.now()}`
    const name = `${minAge}–${maxAge} | ${location} (${(percentage * 100).toFixed(0)}%)`

    const newScenario: Scenario = {
      id,
      name,
      description,
      trait,
      behavior,
      demographic: { ageRange: [minAge, maxAge], location },
      percentage,
      questions: [],
      usersAssigned: [],
      status: "draft",
      keywords: ["vegan", "eco bag", "recycle"],
    }

    set((state) => ({
      scenarios: [...state.scenarios, newScenario],
    }))
  },

  selectQuestions: (scenarioId, questionIds) => {
    set((state) => ({
      scenarios: state.scenarios.map((s) => (s.id === scenarioId ? { ...s, questions: questionIds } : s)),
    }))
  },

  simulateDistribution: (scenarioId) => {
    set((state) => {
      const scenario = state.scenarios.find((s) => s.id === scenarioId)
      if (!scenario) return state

      const eligibleUsers = state.users.filter((u) => {
        const inAgeRange = u.age >= scenario.demographic.ageRange[0] && u.age <= scenario.demographic.ageRange[1]
        const inLocation =
          scenario.demographic.location === "All Locations" || u.location === scenario.demographic.location
        return inAgeRange && inLocation
      })

      const numToAssign = Math.ceil(eligibleUsers.length * scenario.percentage)
      const shuffled = [...eligibleUsers].sort(() => Math.random() - 0.5)
      const assigned = shuffled.slice(0, numToAssign)

      const updatedScenarios = state.scenarios.map((s) =>
        s.id === scenarioId
          ? {
            ...s,
            usersAssigned: assigned.map((u) => u.id),
            status: "sent" as const,
          }
          : s,
      )

      const updatedUsers = state.users.map((u) => {
        if (assigned.find((a) => a.id === u.id)) {
          return {
            ...u,
            assignedScenarios: [...(u.assignedScenarios || []), scenarioId],
          }
        }
        return u
      })

      return { scenarios: updatedScenarios, users: updatedUsers }
    })
  },

  deleteScenario: (scenarioId) => {
    set((state) => ({
      scenarios: state.scenarios.filter((s) => s.id !== scenarioId),
    }))
  },

  updateUserStatus: (userId, status) => {
    set((state) => ({
      users: state.users.map((u) => (u.id === userId ? { ...u, status } : u)),
    }))
  },

  exportTreeAsJSON: () => {
    const state = get()
    return JSON.stringify(state.tree, null, 2)
  },

  exportScenariosAsJSON: () => {
    const state = get()
    return JSON.stringify(state.scenarios, null, 2)
  },

  importTree: (jsonString: string) => {
    try {
      const parsed = JSON.parse(jsonString)
      if (Array.isArray(parsed)) {
        set({ tree: parsed })
        return true
      }
      return false
    } catch {
      return false
    }
  },
}))
