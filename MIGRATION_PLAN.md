# Boolean Calculator - Web to Native Android Migration Plan

## Architecture Overview
- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose (Material 3 with custom styling)
- **Architecture**: MVVM with Clean Domain Engine Layer
- **Offline**: 100% offline, zero network calls or WebView dependencies.

## Key Modules & Package Structure
`com.example.booleancalculator`
├── `domain/`
│   ├── `model/` (Token, ASTNode, TruthTableModel, KMapModel, StepModels)
│   ├── `engine/` (Tokenizer, Parser, BooleanEvaluator, SimplificationEngine, KMapEngine)
├── `viewmodel/`
│   └── `CalculatorViewModel.kt`
├── `ui/`
│   ├── `theme/` (Color.kt, Theme.kt, Type.kt)
│   ├── `components/` (ExpressionDisplay, AnswerDisplay, MainActionButton, VariableAssignmentRow, ControlRow, CustomKeyboard)
│   └── `screens/` (MainScreen, ResultOverlay, FullTruthTableScreen, HowComeScreen, TraceSolveScreen, SimplifySolveScreen, KMapSolveScreen)

## Migration Execution Phases
1. Core Engine implementation (Tokenizer, Parser, AST, Evaluator, Truth Table, Simplification, K-Map).
2. Domain Unit Tests for all engine calculations.
3. Color & Theme Setup matching exact Web CSS palette.
4. Main Screen & Custom Keyboard Components.
5. Result Overlay & Full Truth Table Screen.
6. How'd It Come? (Step-by-step Truth Table animation screen).
7. Solving Steps Screens (Trace solve, Simplification solve, K-Map solve).
8. Responsive Design & Orientation Polish.
