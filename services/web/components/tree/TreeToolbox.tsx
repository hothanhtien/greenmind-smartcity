'use client';

import React, { useState } from 'react';
import { useDraggable } from '@dnd-kit/core';
import { ChevronDown, ChevronRight, GripVertical } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Collapsible, CollapsibleContent, CollapsibleTrigger } from '@/components/ui/collapsible';
import { OCEAN_DATA } from '@/lib/ocean-data';

// Export constants for use in other components
export const TRAITS = Object.keys(OCEAN_DATA) as Array<keyof typeof OCEAN_DATA>;

export const BEHAVIORS = Object.values(OCEAN_DATA).flatMap(data => data.behaviors);

export interface Demographic {
  id: string;
  ageRange: [number, number];
  location: string;
}

export const DEMOGRAPHICS: Demographic[] = [
  { id: 'young-urban', ageRange: [18, 30], location: 'Urban' },
  { id: 'young-rural', ageRange: [18, 30], location: 'Rural' },
  { id: 'middle-urban', ageRange: [31, 50], location: 'Urban' },
  { id: 'middle-rural', ageRange: [31, 50], location: 'Rural' },
  { id: 'senior-urban', ageRange: [51, 70], location: 'Urban' },
  { id: 'senior-rural', ageRange: [51, 70], location: 'Rural' },
];

interface DraggableItemProps {
  id: string;
  children: React.ReactNode;
  type: 'ocean' | 'behavior';
  data: {
    ocean?: string;
    behavior?: string;
  };
}

function DraggableItem({ id, children, type, data }: DraggableItemProps) {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id,
    data: { type, ...data }
  });

  const style = transform ? {
    transform: `translate3d(${transform.x}px, ${transform.y}px, 0)`,
  } : undefined;

  return (
    <div
      ref={setNodeRef}
      style={style}
      className={`cursor-grab active:cursor-grabbing ${isDragging ? 'opacity-50' : ''}`}
      {...listeners}
      {...attributes}
    >
      {children}
    </div>
  );
}

export default function TreeToolbox() {
  const [openSections, setOpenSections] = useState<Record<string, boolean>>({});

  const toggleSection = (key: string) => {
    setOpenSections(prev => ({
      ...prev,
      [key]: !prev[key]
    }));
  };

  return (
    <Card className="w-80 h-fit">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <GripVertical className="w-4 h-4" />
          OCEAN Model Toolbox
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-2">
        {Object.entries(OCEAN_DATA).map(([key, data]) => (
          <Collapsible
            key={key}
            open={openSections[key]}
            onOpenChange={() => toggleSection(key)}
          >
            <div className="space-y-1">
              {/* Main OCEAN category - draggable */}
              <DraggableItem
                id={`ocean-${key}`}
                type="ocean"
                data={{ ocean: key }}
              >
                <div className="flex items-center gap-2 p-2 bg-blue-50 border border-blue-200 rounded-md hover:bg-blue-100 transition-colors">
                  <GripVertical className="w-4 h-4 text-blue-600" />
                  <CollapsibleTrigger asChild>
                    <Button
                      variant="ghost"
                      size="sm"
                      className="flex-1 justify-between p-0 h-auto font-medium text-blue-700"
                    >
                      <span>{key} - {data.label}</span>
                      {openSections[key] ? (
                        <ChevronDown className="w-4 h-4" />
                      ) : (
                        <ChevronRight className="w-4 h-4" />
                      )}
                    </Button>
                  </CollapsibleTrigger>
                </div>
              </DraggableItem>

              {/* Behaviors - draggable */}
              <CollapsibleContent className="space-y-1 ml-4">
                {data.behaviors.map((behavior, index) => (
                  <DraggableItem
                    key={`${key}-${index}`}
                    id={`behavior-${key}-${index}`}
                    type="behavior"
                    data={{ ocean: key, behavior }}
                  >
                    <div className="flex items-center gap-2 p-2 bg-gray-50 border border-gray-200 rounded-md hover:bg-gray-100 transition-colors text-sm">
                      <GripVertical className="w-3 h-3 text-gray-500" />
                      <span className="flex-1">{behavior}</span>
                    </div>
                  </DraggableItem>
                ))}
              </CollapsibleContent>
            </div>
          </Collapsible>
        ))}
      </CardContent>
    </Card>
  );
}
