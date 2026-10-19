import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid,
    Legend,
    ResponsiveContainer,
} from 'recharts'

interface OceanChartProps {
    oceanChartData: any[]
    hoveredBarTrait: string | null
    setHoveredBarTrait: (trait: string | null) => void
    setTooltipPos: (pos: { x: number; y: number } | null) => void
}

export const OceanChart = ({
    oceanChartData,
    hoveredBarTrait,
    setHoveredBarTrait,
    setTooltipPos,
}: OceanChartProps) => {
    return (
        <ResponsiveContainer width="100%" height={300}>
            <BarChart
                data={oceanChartData}
                margin={{ top: 20, right: 20, left: 0, bottom: 20 }}
                onMouseMove={(e: any) => {
                    if (hoveredBarTrait) {
                        const event = e && e.activeTooltipIndex !== undefined ? e : null
                        if (event) {
                            setTooltipPos({
                                x: event.chartX || 0,
                                y: event.chartY || 0,
                            })
                        }
                    }
                }}
                onMouseLeave={() => {
                    setHoveredBarTrait(null)
                    setTooltipPos(null)
                }}
            >
                <CartesianGrid strokeDasharray="3 3" stroke="#e0e7ff" />
                <XAxis dataKey="name" />
                <YAxis domain={[0, 100]} />
                <Legend wrapperStyle={{ paddingTop: '20px' }} />
                <Bar
                    dataKey="Openness"
                    fill={hoveredBarTrait && hoveredBarTrait !== 'Openness' ? '#bfdbfe' : '#3b82f6'}
                    opacity={hoveredBarTrait && hoveredBarTrait !== 'Openness' ? 0.4 : 1}
                    radius={[8, 8, 0, 0]}
                    onMouseEnter={() => setHoveredBarTrait('Openness')}
                    onMouseLeave={() => setHoveredBarTrait(null)}
                />
                <Bar
                    dataKey="Conscientiousness"
                    fill={hoveredBarTrait && hoveredBarTrait !== 'Conscientiousness' ? '#a7f3d0' : '#10b981'}
                    opacity={hoveredBarTrait && hoveredBarTrait !== 'Conscientiousness' ? 0.4 : 1}
                    radius={[8, 8, 0, 0]}
                    onMouseEnter={() => setHoveredBarTrait('Conscientiousness')}
                    onMouseLeave={() => setHoveredBarTrait(null)}
                />
                <Bar
                    dataKey="Extraversion"
                    fill={hoveredBarTrait && hoveredBarTrait !== 'Extraversion' ? '#fed7aa' : '#f59e0b'}
                    opacity={hoveredBarTrait && hoveredBarTrait !== 'Extraversion' ? 0.4 : 1}
                    radius={[8, 8, 0, 0]}
                    onMouseEnter={() => setHoveredBarTrait('Extraversion')}
                    onMouseLeave={() => setHoveredBarTrait(null)}
                />
                <Bar
                    dataKey="Agreeableness"
                    fill={hoveredBarTrait && hoveredBarTrait !== 'Agreeableness' ? '#fbcfe8' : '#ec4899'}
                    opacity={hoveredBarTrait && hoveredBarTrait !== 'Agreeableness' ? 0.4 : 1}
                    radius={[8, 8, 0, 0]}
                    onMouseEnter={() => setHoveredBarTrait('Agreeableness')}
                    onMouseLeave={() => setHoveredBarTrait(null)}
                />
                <Bar
                    dataKey="Neuroticism"
                    fill={hoveredBarTrait && hoveredBarTrait !== 'Neuroticism' ? '#fecaca' : '#ef4444'}
                    opacity={hoveredBarTrait && hoveredBarTrait !== 'Neuroticism' ? 0.4 : 1}
                    radius={[8, 8, 0, 0]}
                    onMouseEnter={() => setHoveredBarTrait('Neuroticism')}
                    onMouseLeave={() => setHoveredBarTrait(null)}
                />
            </BarChart>
        </ResponsiveContainer>
    )
}
