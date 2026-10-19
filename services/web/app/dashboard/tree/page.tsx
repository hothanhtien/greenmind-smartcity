'use client';

import React from 'react';
import { DndContext, DragEndEvent, DragOverlay, DragStartEvent } from '@dnd-kit/core';
import { useState } from 'react';
import TreeToolbox from '@/components/tree/TreeToolbox';
import TreeCanvas from '@/components/tree/TreeCanvas';
import DetailEditor from '@/components/tree/DetailEditor';
import { useOceanModelStore } from '@/store/useOceanModelStore';
import { OCEAN_DATA, type OceanKey } from '@/lib/ocean-data';
import { Card } from '@/components/ui/card';

export default function TreeModelBuilder() {
  const { setSelectedOcean, setSelectedBehavior } = useOceanModelStore();
  const [activeId, setActiveId] = useState<string | null>(null);
  const [draggedItem, setDraggedItem] = useState<any>(null);

  const handleDragStart = (event: DragStartEvent) => {
    setActiveId(event.active.id as string);
    setDraggedItem(event.active.data.current);
  };

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;

    if (over && over.id === 'tree-canvas') {
      const draggedData = active.data.current;

      if (draggedData?.type === 'ocean') {
        // If dragging an OCEAN category
        setSelectedOcean(draggedData.ocean);
        setSelectedBehavior(''); // Reset behavior when changing OCEAN
      } else if (draggedData?.type === 'behavior') {
        // If dragging a behavior
        setSelectedOcean(draggedData.ocean);
        setSelectedBehavior(draggedData.behavior);
      }
    }

    setActiveId(null);
    setDraggedItem(null);
  };

  const handleDragCancel = () => {
    setActiveId(null);
    setDraggedItem(null);
  };

  return (
    <div className="container mx-auto p-6">
      <div className="mb-6">
        <h1 className="text-3xl font-bold">OCEAN Model Builder</h1>
        <p className="text-gray-600 mt-2">
          Drag and drop OCEAN categories and behaviors to create a new model
        </p>
      </div>

      <DndContext
        onDragStart={handleDragStart}
        onDragEnd={handleDragEnd}
        onDragCancel={handleDragCancel}
      >
        <div className="flex gap-6">
          <TreeToolbox />

          <TreeCanvas />

          <DetailEditor />
        </div>

        <DragOverlay>
          {activeId && draggedItem ? (
            <Card className="p-3 bg-white shadow-lg border-2 border-blue-500 opacity-90">
              {draggedItem.type === 'ocean' ? (
                <div className="font-medium text-blue-700">
                  {draggedItem.ocean} - {OCEAN_DATA[draggedItem.ocean as OceanKey]?.label}
                </div>
              ) : (
                <div className="text-sm">
                  <div className="font-medium text-blue-600">{draggedItem.ocean}</div>
                  <div className="text-gray-700">{draggedItem.behavior}</div>
                </div>
              )}
            </Card>
          ) : null}
        </DragOverlay>
      </DndContext>
    </div>
  );
}
