package symboltable;

public enum SymbolType {
  //no type
  ST_VOID,  // only ever used in main
  ST_NULL,

  //Generic overarching types
  ST_VARIABLE,
  ST_METHOD,
  ST_CLASS,

  //Specific Types
  ST_BOOLEAN,
  ST_INT,
  ST_INT_ARR,
  ST_STRING_ARR,  // only ever used in main
  ST_CLASS_VAR,
  ST_CLASS_EXTENDS,
};

