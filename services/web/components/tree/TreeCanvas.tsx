'use client';

import React, { useMemo, useState } from 'react';
import { useDroppable } from '@dnd-kit/core';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Trash2, Pencil, Check, X } from 'lucide-react';
import { useOceanModelStore } from '@/store/useOceanModelStore';
import { OCEAN_DATA, type OceanKey } from '@/lib/ocean-data';

export default function TreeCanvas() {
  const {
    selectedOcean,
    selectedBehavior,
    setSelectedOcean,
    setSelectedBehavior,
  } = useOceanModelStore();

  const { isOver, setNodeRef } = useDroppable({
    id: 'tree-canvas',
  });

  // State để chỉnh sửa behavior
  const [isEditingBehavior, setIsEditingBehavior] = useState(false);
  const [editBehaviorValue, setEditBehaviorValue] = useState('');

  // Lấy TẤT CẢ behaviors từ tất cả traits
  const allBehaviors = useMemo(() => {
    const behaviors: { ocean: string; behavior: string }[] = [];
    Object.entries(OCEAN_DATA).forEach(([ocean, data]) => {
      data.behaviors.forEach((behavior) => {
        behaviors.push({ ocean, behavior });
      });
    });
    return behaviors;
  }, []);

  const clearSelection = () => {
    setSelectedOcean('');
    setSelectedBehavior('');
    setIsEditingBehavior(false);
  };

  const handleBehaviorChange = (behavior: string) => {
    setSelectedBehavior(behavior);
    setEditBehaviorValue(behavior);
  };

  const handleStartEditBehavior = () => {
    setEditBehaviorValue(selectedBehavior);
    setIsEditingBehavior(true);
  };

  const handleConfirmEditBehavior = () => {
    if (editBehaviorValue.trim()) {
      setSelectedBehavior(editBehaviorValue.trim());
    }
    setIsEditingBehavior(false);
  };

  const handleCancelEditBehavior = () => {
    setIsEditingBehavior(false);
    setEditBehaviorValue(selectedBehavior);
  };

  return (
    <Card className="flex-1 min-h-[400px]">
      <CardHeader>
        <CardTitle className="flex items-center justify-between">
          Tree Canvas
          {(selectedOcean || selectedBehavior) && (
            <Button
              variant="outline"
              size="sm"
              onClick={clearSelection}
              className="text-red-600 hover:text-red-700"
            >
              <Trash2 className="w-4 h-4 mr-1" />
              Clear
            </Button>
          )}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <div
          ref={setNodeRef}
          className={`
            min-h-[300px] border-2 border-dashed rounded-lg p-6 flex flex-col items-center justify-center
            ${isOver ? 'border-blue-500 bg-blue-50' : 'border-gray-300'}
            ${selectedOcean || selectedBehavior ? 'border-green-500 bg-green-50' : ''}
          `}
        >
          {!selectedOcean && !selectedBehavior ? (
            <div className="text-center text-gray-500">
              <div className="text-lg font-medium mb-2">
                Drag and drop OCEAN category or behavior here
              </div>
              <div className="text-sm">
                You can drag and drop items from the Toolbox on the left
              </div>
            </div>
          ) : (
            <div className="w-full max-w-md space-y-4">
              <div className="text-center">
                <h3 className="text-lg font-semibold mb-4">Selected Model</h3>
              </div>

              {/* OCEAN Category */}
              <div className="space-y-2">
                <label className="text-sm font-medium">OCEAN Category:</label>
                <div className="p-3 bg-blue-100 border border-blue-300 rounded-md">
                  <div className="font-medium text-blue-800">
                    {selectedOcean} - {selectedOcean && OCEAN_DATA[selectedOcean as OceanKey]?.label}
                  </div>
                </div>
              </div>

              {/* Behavior Selection & Edit */}
              <div className="space-y-2">
                <label className="text-sm font-medium">Behavior:</label>
                {selectedBehavior ? (
                  isEditingBehavior ? (
                    <div className="space-y-2">
                      <Input
                        value={editBehaviorValue}
                        onChange={(e) => setEditBehaviorValue(e.target.value)}
                        placeholder="Enter behavior..."
                        autoFocus
                        onKeyDown={(e) => {
                          if (e.key === 'Enter') {
                            handleConfirmEditBehavior();
                          } else if (e.key === 'Escape') {
                            handleCancelEditBehavior();
                          }
                        }}
                      />
                      <div className="flex gap-2">
                        <Button size="sm" onClick={handleConfirmEditBehavior} className="flex-1">
                          <Check className="w-4 h-4 mr-1" />
                          Confirm
                        </Button>
                        <Button size="sm" variant="outline" onClick={handleCancelEditBehavior} className="flex-1">
                          <X className="w-4 h-4 mr-1" />
                          Cancel
                        </Button>
                      </div>
                    </div>
                  ) : (
                    <div className="p-3 bg-green-100 border border-green-300 rounded-md flex items-center justify-between">
                      <div className="font-medium text-green-800">{selectedBehavior}</div>
                      <Button size="sm" variant="ghost" onClick={handleStartEditBehavior}>
                        <Pencil className="w-4 h-4" />
                      </Button>
                    </div>
                  )
                ) : selectedOcean ? (
                  <Select value={selectedBehavior} onValueChange={handleBehaviorChange}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select behavior..." />
                    </SelectTrigger>
                    <SelectContent>
                      {allBehaviors.map(({ behavior }, index) => (
                        <SelectItem key={`${selectedOcean}-${index}`} value={behavior}>
                          {behavior}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                ) : (
                  <div className="p-3 bg-gray-100 border border-gray-300 rounded-md text-gray-500">
                    Select OCEAN category first
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
