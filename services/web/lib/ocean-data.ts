export const OCEAN_DATA = {
    O: {
        label: 'Openness',
        behaviors: [
            'modibility behavior (adaptive/change behavior)'
        ]
    },
    C: {
        label: 'Conscientiousness',
        behaviors: [
            'goal setting behavior'
        ]
    },
    E: {
        label: 'Extraversion',
        behaviors: [
            'eating behavior',
            'social interaction'
        ]
    },
    A: {
        label: 'Agreeableness',
        behaviors: [
            'purchasing behavior'
        ]
    },
    N: {
        label: 'Neuroticism',
        behaviors: [
            'affective state (emotional state)'
        ]
    }
} as const;

export type OceanKey = keyof typeof OCEAN_DATA;
