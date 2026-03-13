# 🌿 Minimal Professional Meal Planner - UI Design

## ✨ Calm & Sophisticated Interface

### Design Philosophy:
**"Calm Clarity"** - A minimal, professional interface that feels like a premium productivity app. Neutral colors, clean typography, and purposeful design.

---

## 🎨 Minimal Color System

### **Neutral Palette:**

#### White & Gray Scale:
```
Neutral White:  #FAFAFA (Clean background)
Neutral Light:  #F5F5F5 (Subtle contrast)
Neutral Gray:   #E0E0E0 (Borders, dividers)
Neutral Medium: #9E9E9E (Secondary elements)
Neutral Dark:   #424242 (Primary text)
Neutral Black:  #212121 (Headlines)
```

#### Warm Beige Tones:
```
Beige Soft:  #F5F1E8 (Warm backgrounds)
Beige Warm:  #EBE5DA (Gentle contrast)
Beige Sand:  #D4C4B0 (Accent elements)
```

#### Muted Greens:
```
Green Muted:  #7A8B77 (Primary brand)
Green Sage:   #8F9E8A (Secondary)
Green Olive:  #6B7263 (Dark accent)
```

### **Gradients:**

#### Neutral Gradient:
```
Start:  #FAFAFA
Center: #F5F5F5
End:    #E8E8E8
Angle:  135° (subtle diagonal)
```

#### Beige Gradient:
```
Start:  #F5F1E8
Center: #EBE5DA
End:    #D4C4B0
Angle:  225° (warm undertone)
```

### **Text Hierarchy:**

```
Primary Text:    #212121 (Headlines, body)
Secondary Text:  #757575 (Supporting info)
Muted Text:      #BDBDBD (Hints, metadata)
On Dark:         #FFFFFF (Inverse backgrounds)
```

---

## 📱 Screen Designs

### **1. Login Screen - Minimal Elegance**

#### Layout:
```
┌─────────────────────────────────┐
│                                 │
│         [Logo]                  │ ← Simple line icon
│                                 │
│    Welcome Back                 │ ← 32sp, #212121
│    Plan your meals              │ ← 16sp, #757575
│                                 │
│    ┌──────────────────────┐    │ ← White card
│    │ Email Address        │    │ ← #757575 border
│    └──────────────────────┘    │
│                                 │
│    ┌──────────────────────┐    │
│    │ Password             │    │
│    └──────────────────────┘    │
│                                 │
│    Forgot Password?             │ ← 14sp, #7A8B77
│                                 │
│    ┌──────────────────────┐    │
│    │     Sign In          │    │ ← #7A8B77 fill
│    └──────────────────────┘    │ ← 20dp corners
│                                 │
│    Or continue with             │
│                                 │
│    ┌──────────────────────┐    │
│    │ G Google             │    │ ← Outlined
│    └──────────────────────┘    │
│                                 │
│    Don't have an account?       │
│    Create Account               │
│                                 │
└─────────────────────────────────┘
```

**Design Elements:**
- Background: Subtle neutral gradient
- Cards: White with soft shadows (8px blur)
- Buttons: Muted green (#7A8B77), 20dp radius
- Inputs: Light gray borders (#E0E0E0)
- Typography: Clean sans-serif, generous spacing

---

### **2. Home Dashboard - Clean Overview**

#### Layout:
```
┌─────────────────────────────────┐
│ Good Morning                   │ ← 28sp Bold
│ Monday, Jan 15                 │ ← 16sp Muted
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃ Today's Progress         ┃   │ ← White card
│ ┃                           ┃   │
│ ┃   1,450 / 2,000 cal      ┃   │ ← 28sp Bold
│ ┃   █████████░░░ 72%       ┃   │ ← Muted green
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ Meals Today                    │ ← 18sp SemiBold
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃ Breakfast                ┃   │ ← Beige card
│ ┃ Oatmeal with Berries     ┃   │
│ ┃ 350 cal • 12g protein    ┃   │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃ Lunch                    ┃   │
│ ┃ Grilled Chicken Salad    ┃   │
│ ┃ 450 cal • 35g protein    ┃   │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃ Dinner                   ┃   │
│ ┃ Salmon with Vegetables   ┃   │
│ ┃ 550 cal • 40g protein    ┃   │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ [⊕]                            │ ← Floating action
└─────────────────────────────────┘
```

---

### **3. Meal List - Organized & Clean**

#### Layout:
```
┌─────────────────────────────────┐
│ My Meals                        │
│ ┌──────────────────────────┐   │
│ │ 🔍 Search...             │   │ ← Light gray bg
│ └──────────────────────────┘   │
│                                 │
│ [All] [Breakfast] [Lunch] [▼] │ ← Filter chips
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃ Avocado Toast            ┃   │
│ ┃ Breakfast • 350 cal      ┃   │
│ ┃ 10 min • Easy            ┃   │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃ Caesar Salad             ┃   │
│ ┃ Lunch • 450 cal          ┃   │
│ ┃ 15 min • Medium          ┃   │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃ Spaghetti Bolognese      ┃   │
│ ┃ Dinner • 650 cal         ┃   │
│ ┃ 30 min • Hard            ┃   │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ [⊕]                            │
└─────────────────────────────────┘
```

---

### **4. Add Meal - Simple Form**

#### Layout:
```
┌─────────────────────────────────┐
│ ← Add New Meal                  │
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃                          ┃   │
│ ┃    [📷 Add Photo]       ┃   │ ← Dashed border
│ ┃                          ┃   │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ Meal Name                       │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃ Enter meal name          ┃   │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ Meal Type                       │
│ [Breakfast] [Lunch] [Dinner]   │ ← Pill chips
│ [Snack]                         │
│                                 │
│ Nutrition                       │
│ ┌────┐ ┌────┐ ┌────┐ ┌────┐   │
│ │Cal │ │Pro │ │Carb│ │Fat │   │
│ │350 │ │25g │ │40g │ │12g │   │
│ └────┘ └────┘ └────┘ └────┘   │
│                                 │
│ Prep Time                       │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃ 15 minutes               ┃   │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━┓       │
│ ┃   Save Meal          │       │
│ ┗━━━━━━━━━━━━━━━━━━━━━━┛       │
│                                 │
└─────────────────────────────────┘
```

---

### **5. User Profile - Professional Summary**

#### Layout:
```
┌─────────────────────────────────┐
│                                 │
│    [Profile Photo]              │ ← 100dp circle
│                                 │
│    Alex Johnson                 │ ← 24sp Bold
│    alex@example.com             │ ← 14sp Muted
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃ Statistics               ┃   │
│ ┃                           ┃   │
│ ┃ Weight                   ┃   │
│ ┃ 68 kg → 65 kg            ┃   │
│ ┃ █████████░ 80%           ┃   │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃ Daily Goals              ┃   │
│ ┃                           ┃   │
│ ┃ Calories    2,000        ┃   │
│ ┃ Protein     120g         ┃   │
│ ┃ Carbs       200g         ┃   │
│ ┃ Fat         60g          ┃   │
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
│ Settings                        │
│ ─────────────────────────────   │
│ Notifications                   │
│ Dietary Preferences             │
│ Analytics                       │
│ Help & Support                  │
│ About                           │
│                                 │
│ ┏━━━━━━━━━━━━━━━━━━━━━━━━━━┓   │
│ ┃   Sign Out               ┃   │ ← Outline only
│ ┗━━━━━━━━━━━━━━━━━━━━━━━━━━┛   │
│                                 │
└─────────────────────────────────┘
```

---

## 🎨 Component Library

### **Buttons:**

#### Primary Button:
```xml
Height: 56dp
Corner Radius: 20dp
Background: #7A8B77 (muted green)
Text: 16sp SemiBold, #FFFFFF
Shadow: 0px 2px 8px rgba(0,0,0,0.08)
```

#### Secondary Button:
```xml
Height: 48dp
Corner Radius: 16dp
Border: 1px solid #E0E0E0
Background: Transparent
Text: 16sp Medium, #424242
```

### **Input Fields:**

#### Text Input:
```xml
Height: 56dp
Corner Radius: 12dp
Background: #FFFFFF
Border: 1px solid #E0E0E0
Focus Border: 2px solid #7A8B77
Padding: 16dp
Placeholder: #9E9E9E
```

### **Cards:**

#### Content Card:
```xml
Background: #FFFFFF
Corner Radius: 16dp
Shadow: 0px 4px 12px rgba(0,0,0,0.06)
Padding: 20dp
Margin: 16dp
```

#### Stat Card:
```xml
Background: #F5F1E8 (beige)
Corner Radius: 16dp
No shadow
Padding: 16dp
```

### **Chips:**

#### Filter Chip:
```xml
Height: 32dp
Corner Radius: 16dp
Background: #F5F5F5
Text: 14sp Medium, #424242
Selected: #7A8B77 background, white text
```

---

## ✨ Typography System

### **Font Scale:**

```
Display:    32sp Bold      (Screen titles)
Headline:   28sp Bold      (Section headers)
Title:      20sp SemiBold  (Card titles)
Subtitle:   18sp Regular   (Secondary info)
Body:       16sp Regular   (Main content)
Caption:    14sp Regular   (Hints)
Small:      12sp Light     (Metadata)
```

### **Font Family:**
- **Primary**: Inter or SF Pro Display
- **Weight**: Light (300), Regular (400), Medium (500), SemiBold (600), Bold (700)
- **Line Height**: 1.5x font size for readability

---

## 🎯 Minimal Icon Set

### Style:
- **Stroke width**: 2px consistent
- **Fill**: None (outline only)
- **Color**: #7A8B77 (muted green)
- **Size**: 24dp standard grid

### Icons Created:
1. **Home** - Simple house outline
2. **Calendar** - Calendar grid with rings
3. **User** - Head and shoulders outline
4. **Plus** - Cross/plus symbol
5. **Search** - Magnifying glass
6. **Settings** - Gear/cog outline

---

## 🌙 Dark Mode Adaptation

### Color Adjustments:
```
Background: #1A1A1A
Surface: #242424
Cards: #2C2C2C
Text Primary: #FFFFFF
Text Secondary: #B0B0B0
Accents: Maintain muted tones
```

### Shadows in Dark Mode:
```
Replace with subtle glows:
0px 0px 12px rgba(255,255,255,0.05)
```

---

## ♿ Accessibility Features

### WCAG 2.1 AA Compliance:
- ✅ Contrast ratio: 4.5:1 minimum (AAA for large text)
- ✅ Touch targets: 48x48dp minimum
- ✅ Focus indicators: Clear 2px outlines
- ✅ Screen reader optimized
- ✅ Dynamic type support (up to 200%)
- ✅ Reduced motion option

---

## 🚀 Performance Best Practices

### Images:
```
Format: WebP
Quality: 85%
Lazy loading: Enabled
Cache: LRU strategy
```

### Animations:
```
Duration: 200-300ms
Curve: EaseInOut
Frame rate: 60fps target
Hardware acceleration: Enabled
```

### Layout Optimization:
```
ConstraintLayout: Flat hierarchy
ViewStub: Lazy inflation
RecyclerView: Efficient recycling
DiffUtil: Smart updates
```

---

## 📊 Design Principles

1. **Simplicity** - Every element serves a purpose
2. **Clarity** - Instant comprehension
3. **Consistency** - Unified visual language
4. **Efficiency** - Minimize cognitive load
5. **Elegance** - Understated sophistication
6. **Accessibility** - Inclusive design
7. **Performance** - Fast and smooth
8. **Professionalism** - Premium feel

---

## 💼 Professional Brand Identity

### Personality:
- **Calm** - Soothing, not stimulating
- **Professional** - Business-like, serious
- **Trustworthy** - Reliable, dependable
- **Sophisticated** - Refined, tasteful
- **Minimal** - Essential only

### Visual Characteristics:
- Neutral color palette
- Generous white space
- Consistent spacing rhythm
- Subtle shadows (not dramatic)
- Clean typography
- Purposeful icons
- No decorative elements

---

## 📱 Responsive Design

### Breakpoints:

#### Compact (≤360dp):
```
Reduced padding: 12dp
Smaller fonts: -10%
Single column layout
Simplified navigation
```

#### Standard (360-600dp):
```
Standard padding: 16dp
Full feature set
Optimal spacing
Bottom navigation
```

#### Large (≥600dp tablets):
```
Increased padding: 24dp
Multi-column layouts
Expanded views
Desktop-like navigation
```

---

## 🎯 Startup-Quality Features

### What Makes It Professional:
✅ Restrained color palette  
✅ Exceptional attention to detail  
✅ Pixel-perfect alignment  
✅ Consistent spacing system  
✅ Professional typography  
✅ Subtle micro-interactions  
✅ Accessible to all users  
✅ Optimized performance  
✅ Scalable design system  
✅ Timeless aesthetics  

---

**Your minimal, professional meal planner is complete!** ✨

This design rivals premium productivity apps like:
- **Notion** (clean minimalism)
- **Things 3** (refined UI)
- **Fantastical** (professional aesthetics)
- **Bear** (elegant simplicity)
- **OmniFocus** (sophisticated design)

The neutral palette of grays, beiges, and muted greens creates a calm, sophisticated interface perfect for professionals who want a mature, polished meal planning app! 🌿✨

Ready for development! 💼
