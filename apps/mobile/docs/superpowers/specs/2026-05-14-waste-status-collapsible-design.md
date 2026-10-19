# Household Waste Status - Collapsible Sections + Date Filter

**Date:** 2026-05-14
**Status:** Approved for implementation

---

## Overview

Modify `WasteStatusScreen.kt` to make the 3 status sections (Sorted, Brought Out, Collected) collapsible, and add date quick filters to the filter bottom sheet.

---

## 1. Filter Sheet Changes (`WasteStatusFilterSheet.kt`)

### Remove
- `SortOrder` enum and related UI (NEWEST_FIRST, OLDEST_FIRST)

### Add
New `DateFilter` enum:
```kotlin
enum class DateFilter {
    TODAY,
    LAST_7_DAYS,
    LAST_30_DAYS,
    ALL_TIME
}
```

### New Layout
- **Date filter section** (replaces sort section):
  - Label: "Filter by date"
  - 4 chips: "Today", "7 days", "30 days", "All Time"
  - Colors: selected = Green800, unselected = light gray background

- **Apply button** remains unchanged

---

## 2. WasteStatusScreen Changes

### State
- Replace `selectedSortOrder: SortOrder` with `selectedDateFilter: DateFilter`
- Default: `DateFilter.ALL_TIME`

### Filter Logic (`applyFilters`)
```kotlin
private fun applyFilters(
    scans: List<ScanDetailData>,
    status: WasteSortStatus?,
    dateFilter: DateFilter,
    onResult: (List<ScanDetailData>) -> Unit,
) {
    var result = scans

    status?.let { s ->
        result = result.filter { it.status == s }
    }

    val now = java.time.LocalDate.now()
    val cutoff = when (dateFilter) {
        DateFilter.TODAY -> now
        DateFilter.LAST_7_DAYS -> now.minusDays(7)
        DateFilter.LAST_30_DAYS -> now.minusDays(30)
        DateFilter.ALL_TIME -> null
    }
    if (cutoff != null) {
        result = result.filter { scan ->
            scan.createdAt?.take(10)?.let { dateStr ->
                java.time.LocalDate.parse(dateStr) >= cutoff
            } ?: true
        }
    }

    onResult(result.sortedByDescending { it.createdAt })
}
```

**Note:** Sort is always newest-first. `SortOrder` enum is removed from `WasteStatusFilterSheet.kt`.

### Collapsible Sections

Add `expandedStates` map in the composable:
```kotlin
var sectionExpanded by remember { mutableStateOf(
    mapOf(
        WasteSortStatus.SORTED to true,
        WasteSortStatus.BRINGOUTED to true,
        WasteSortStatus.COLLECTED to true
    )
)}
```

**StatusSectionHeader** signature change:
```kotlin
@Composable
private fun StatusSectionHeader(
    title: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    expanded: Boolean,
    onToggle: () -> Unit,
)
```

Header Row becomes clickable:
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(bgColor)
        .clickable { onToggle() }
        .padding(horizontal = 12.dp, vertical = 8.dp),
    ...
) {
    Text(title, ...)
    Spacer(Modifier.weight(1f))
    Text("$count", ...)       // count badge
    Icon(                     // chevron
        if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
        ...
    )
}
```

**Section rendering** wraps content in `AnimatedVisibility`:
```kotlin
if (expandedSection == status) {
    items(section) { scan -> ... }
}
```

### Collapsed Section Styling
When collapsed, section still shows header with:
- Title + count badge
- Chevron pointing right (▶)
- Tappable to expand
- Count badge uses same color style as expanded state

---

## 3. File Changes Summary

| File | Change |
|------|--------|
| `WasteStatusFilterSheet.kt` | Remove `SortOrder`, add `DateFilter` enum, replace sort UI with date filter UI |
| `WasteStatusScreen.kt` | Add collapsible state, update filter logic, update section headers to be collapsible, remove sort order |

---

## 4. Default Behavior

- All 3 sections expanded by default
- Date filter defaults to "All Time"
- Clicking a collapsed section header expands it
- Clicking an expanded section header collapses it
- Filter sheet Apply button triggers re-filter with new date filter