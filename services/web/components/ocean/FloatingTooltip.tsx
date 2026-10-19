interface FloatingTooltipProps {
    hoveredBarTrait: string | null
    tooltipPos: { x: number; y: number } | null
    oceanChartData: any[]
}

export const FloatingTooltip = ({
    hoveredBarTrait,
    tooltipPos,
    oceanChartData,
}: FloatingTooltipProps) => {
    if (!hoveredBarTrait || !tooltipPos) return null

    const traitValue = oceanChartData[0]?.[hoveredBarTrait as keyof typeof oceanChartData[0]]

    return (
        <div
            className="fixed bg-white border-2 border-gray-400 rounded-lg p-3 shadow-xl pointer-events-none z-50"
            style={{
                left: `${tooltipPos.x}px`,
                top: `${tooltipPos.y}px`,
                transform: 'translate(-50%, -140%)',
            }}
        >
            <div className="text-sm font-bold text-gray-800">
                {hoveredBarTrait}: {traitValue}
            </div>
        </div>
    )
}
