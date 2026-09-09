//https://github.com/alphagov/accessible-autocomplete/issues/535
// Accessible autocomplete does not have a TypeScript definition file, so we need to define
// the types ourselves. This also means importing the library in HTML as well.

interface AccessibleAutocompleteOptions {
  element: HTMLElement;
  id: string;
  source:
      | string[]
      | ((query: string, populateResults: (results: string[]) => void) => void);
  autoselect?: boolean;
  confirmOnBlur?: boolean;
  cssNamespace?: string;
  defaultValue?: string;
  displayMenu?: 'inline' | 'overlay';
  minLength?: number;
  name?: string;
  placeholder?: string;
  onConfirm?: (confirmed: string) => void;
  required?: boolean;
  showAllValues?: boolean;
  dropdownArrow?(props: { className: string }): string | ReactElement;
  showNoOptionsFound?: boolean;
  templates?: {
    inputValue?: (result: string) => string;
    suggestion?: (result: string) => string;
  };
  tNoResults?: () => string;
  tStatusQueryTooShort?: (minQueryLength: number) => string;
  tStatusNoResults?: () => string;
  tStatusSelectedOption?: (
      selectedOption: string,
      length: number,
      index: number
  ) => string;
  tStatusResults?: (
      length: number,
      contentSelectedOption: string
  ) => string;
  tAssistiveHint?: () => string;
}

interface EnhanceSelectElementOptions
    extends Partial<AccessibleAutocompleteOptions> {
  selectElement: HTMLSelectElement;
  defaultValue?: string;
  autoselect?: boolean;
  preserveNullOptions?: boolean;
  showAllValues?: boolean;
  allowEmpty?: boolean;
  inputClasses?: string;
}

interface AccessibleAutocomplete {
  (options: AccessibleAutocompleteOptions): void;

  enhanceSelectElement(options: EnhanceSelectElementOptions): void;
}

declare const accessibleAutocomplete: AccessibleAutocomplete;
