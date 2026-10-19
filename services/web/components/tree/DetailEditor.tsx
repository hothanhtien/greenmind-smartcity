'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';
import { Checkbox } from '@/components/ui/checkbox';
import { Badge } from '@/components/ui/badge';
import { Loader2, Sparkles, Save, Plus, X } from 'lucide-react';
import { useOceanModelStore } from '@/store/useOceanModelStore';
import { toast } from 'sonner';
import { createModel, generateKeywords } from '@/lib/auth';

export default function DetailEditor() {
  const router = useRouter();
  const {
    selectedOcean,
    selectedBehavior,
    context,
    keywords,
    generatedKeywords,
    isGenerating,
    setContext,
    setPopulation,
    setKeywords,
    setGeneratedKeywords,
    setIsGenerating,
    addModel,
    reset,
  } = useOceanModelStore();

  const [selectedGeneratedKeyword, setSelectedGeneratedKeyword] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [newLocation, setNewLocation] = useState('');

  const genderOptions = [
    { value: 'male', label: 'Male' },
    { value: 'female', label: 'Female' },
    { value: 'all', label: 'All' },
  ];

  const isFormValid = selectedOcean && selectedBehavior && context.population.gender.length > 0 && context.population.locations.length > 0;

  const handleAddLocation = () => {
    if (newLocation.trim() && !context.population.locations.includes(newLocation.trim())) {
      setPopulation({
        locations: [...context.population.locations, newLocation.trim()],
      });
      setNewLocation('');
    }
  };

  const handleRemoveLocation = (location: string) => {
    setPopulation({
      locations: context.population.locations.filter((l) => l !== location),
    });
  };

  const handleGenderSelect = (gender: string) => {
    // Single select - only one option at a time
    if (gender === 'all') {
      setPopulation({
        gender: ['male', 'female'],
      });
    } else {
      setPopulation({
        gender: [gender],
      });
    }
  };

  // Get current selected gender option
  const getSelectedGenderOption = () => {
    if (context.population.gender.length === 2 &&
      context.population.gender.includes('male') &&
      context.population.gender.includes('female')) {
      return 'all';
    }
    if (context.population.gender.length === 1) {
      return context.population.gender[0];
    }
    return '';
  };

  // Create age_range from age_from and age_to
  const getAgeRange = () => `${context.population.age_from}-${context.population.age_to}`;

  const handleGenerateKeywords = async () => {
    if (!isFormValid) {
      toast.error('Please fill in all required fields: OCEAN, behavior and context');
      return;
    }

    setIsGenerating(true);
    try {
      const data = await generateKeywords({
        ocean: selectedOcean,
        behavior: selectedBehavior,
        context: {
          population: {
            age_range: getAgeRange(),
            gender: context.population.gender,
            locations: context.population.locations,
            urban: context.population.urban,
          },
          setting: context.setting,
          event: context.event,
        },
      });

      setGeneratedKeywords(data.output.keywords);
      toast.success('Keywords generated successfully!');
    } catch (error) {
      console.error('Error generating keywords:', error);
      toast.error('An error occurred while generating keywords');
    } finally {
      setIsGenerating(false);
    }
  };

  const handleSaveModel = async () => {
    if (!isFormValid) {
      toast.error('Please fill in all required fields');
      return;
    }

    const finalKeywords = selectedGeneratedKeyword || keywords;
    if (!finalKeywords.trim()) {
      toast.error('Please enter or select keywords');
      return;
    }

    setIsSaving(true);
    try {
      const payload = {
        ocean: selectedOcean,
        behavior: selectedBehavior,
        keyword: finalKeywords,
        context: {
          population: {
            age_range: getAgeRange(),
            gender: context.population.gender,
            locations: context.population.locations,
            urban: context.population.urban,
          },
          setting: context.setting,
          event: context.event,
        },
      };

      await createModel(payload);

      const savedModel = {
        id: Date.now().toString(),
        ocean: selectedOcean,
        behavior: selectedBehavior,
        context: payload.context,
        keywords: finalKeywords,
      };

      addModel(savedModel);
      reset();
      setSelectedGeneratedKeyword('');

      toast.success(
        `🎉 Model saved successfully!\n📊 OCEAN: ${selectedOcean}\n🎯 Behavior: ${selectedBehavior}`,
        {
          duration: 5000,
          style: {
            background: '#10B981',
            color: 'white',
            fontWeight: '500'
          }
        }
      );

      router.push('/dashboard/questions');
    } catch (error) {
      console.error('Error saving model:', error);
      toast.error("An error occurred while saving the model. Please try again!", {
        duration: 4000,
        style: {
          background: '#EF4444',
          color: 'white'
        }
      });
    } finally {
      setIsSaving(false);
    }
  };

  const handleKeywordSelection = (keyword: string) => {
    setSelectedGeneratedKeyword(keyword);
    setKeywords(keyword);
  };

  return (
    <div className="w-96 space-y-4">
      {/* Context Form */}
      <Card>
        <CardHeader>
          <CardTitle>Context</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {/* Population Section */}
          <div className="space-y-4 p-3 border rounded-lg bg-muted/30">
            <h4 className="text-sm font-medium">Population</h4>

            {/* Age Range - 2 inputs */}
            <div className="space-y-2">
              <Label>Age Range</Label>
              <div className="flex items-center gap-2">
                <Input
                  type="number"
                  placeholder="From"
                  value={context.population.age_from}
                  onChange={(e) => setPopulation({ age_from: e.target.value })}
                  className="w-24"
                  min={0}
                  max={120}
                />
                <span className="text-muted-foreground">-</span>
                <Input
                  type="number"
                  placeholder="To"
                  value={context.population.age_to}
                  onChange={(e) => setPopulation({ age_to: e.target.value })}
                  className="w-24"
                  min={0}
                  max={120}
                />
                <span className="text-sm text-muted-foreground">years</span>
              </div>
            </div>

            {/* Gender Single Select */}
            <div className="space-y-2">
              <Label>Gender</Label>
              <div className="flex flex-wrap gap-2">
                {genderOptions.map((option) => {
                  const isSelected = getSelectedGenderOption() === option.value;

                  return (
                    <div
                      key={option.value}
                      className={`flex items-center gap-2 px-3 py-2 border rounded-md cursor-pointer transition-colors ${isSelected
                          ? 'bg-primary text-primary-foreground border-primary'
                          : 'hover:bg-muted'
                        }`}
                      onClick={() => handleGenderSelect(option.value)}
                    >
                      <div className={`w-4 h-4 rounded-full border-2 flex items-center justify-center ${isSelected ? 'border-primary-foreground' : 'border-muted-foreground'
                        }`}>
                        {isSelected && (
                          <div className="w-2 h-2 rounded-full bg-primary-foreground" />
                        )}
                      </div>
                      <span className="text-sm">{option.label}</span>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Locations Multi-input */}
            <div className="space-y-2">
              <Label>Locations</Label>
              <div className="flex gap-2">
                <Input
                  placeholder="Enter location (e.g., Quang Nam)"
                  value={newLocation}
                  onChange={(e) => setNewLocation(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') {
                      e.preventDefault();
                      handleAddLocation();
                    }
                  }}
                />
                <Button type="button" variant="secondary" size="icon" onClick={handleAddLocation}>
                  <Plus className="h-4 w-4" />
                </Button>
              </div>
              {context.population.locations.length > 0 && (
                <div className="flex flex-wrap gap-2 mt-2">
                  {context.population.locations.map((location) => (
                    <Badge key={location} variant="secondary" className="flex items-center gap-1">
                      {location}
                      <X
                        className="h-3 w-3 cursor-pointer hover:text-destructive"
                        onClick={() => handleRemoveLocation(location)}
                      />
                    </Badge>
                  ))}
                </div>
              )}
            </div>

            {/* Urban Checkbox */}
            <div className="flex items-center space-x-2">
              <Checkbox
                id="urban"
                checked={context.population.urban}
                onCheckedChange={(checked) =>
                  setPopulation({ urban: checked as boolean })
                }
              />
              <Label htmlFor="urban" className="cursor-pointer">
                Urban
              </Label>
            </div>
          </div>

          {/* Setting */}
          <div className="space-y-2">
            <Label htmlFor="setting">Setting</Label>
            <Input
              id="setting"
              placeholder="Enter setting (e.g., cleaning water sources)"
              value={context.setting}
              onChange={(e) => setContext({ setting: e.target.value })}
            />
          </div>

          {/* Event */}
          <div className="space-y-2">
            <Label htmlFor="event">Event</Label>
            <Input
              id="event"
              placeholder="Enter event (e.g., Green Sunday in rural area)"
              value={context.event}
              onChange={(e) => setContext({ event: e.target.value })}
            />
          </div>
        </CardContent>
      </Card>

      {/* Keywords Form */}
      <Card>
        <CardHeader>
          <CardTitle>Keywords</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="keywords">Current Keywords</Label>
            <Textarea
              id="keywords"
              placeholder="Enter keywords (separated by comma)"
              value={keywords}
              onChange={(e) => setKeywords(e.target.value)}
              rows={3}
            />
          </div>

          <Button
            onClick={handleGenerateKeywords}
            disabled={!isFormValid || isGenerating}
            className="w-full"
          >
            {isGenerating ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Generating keywords...
              </>
            ) : (
              <>
                <Sparkles className="w-4 h-4 mr-2" />
                Generate Keywords
              </>
            )}
          </Button>

          {/* Generated Keywords */}
          {generatedKeywords.length > 0 && (
            <div className="space-y-2">
              <Label>Generated Keywords:</Label>
              <Select
                value={selectedGeneratedKeyword}
                onValueChange={handleKeywordSelection}
              >
                <SelectTrigger>
                  <SelectValue placeholder="Select a generated keyword" />
                </SelectTrigger>
                <SelectContent>
                  {generatedKeywords.map((keyword, index) => (
                    <SelectItem key={index} value={keyword}>
                      {keyword}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}

          {/* Save Model Button */}
          <Button
            onClick={handleSaveModel}
            disabled={!isFormValid || isSaving}
            className="w-full"
            variant="default"
          >
            {isSaving ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                Saving model...
              </>
            ) : (
              <>
                <Save className="w-4 h-4 mr-2" />
                Save Model
              </>
            )}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}

