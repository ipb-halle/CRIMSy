import { useState, useRef, useEffect } from "react";
import { SearchType, MaterialType, 
  SearchTypeFromJSON, MaterialTypeFromJSON } 
  from "../../adapters/api";
import "../../assets/css/searchDropdown.css";



interface Option {
  label: string;
  value: string;
  subOptions?: string[];
}

interface Props {
  onSearchTypesChange: (types: SearchType[]) => void;
  onMaterialTypesChange: (types: MaterialType[]) => void;
}

export const SearchDropdown = ({
  onSearchTypesChange,
  onMaterialTypesChange,
}: Props) => {
  const [open, setOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement | null>(null);

  const [selectedOptions, setSelectedOptions] = useState<string[]>([]);
  const [selectedSubOptions, setSelectedSubOptions] = useState<string[]>([]);

  const options: Option[] = [
    {
      label: "Materials",
      value: SearchType.Materials,
      subOptions: [MaterialType.Structure, MaterialType.Sequence, MaterialType.Biomaterial, MaterialType.Composition]
    },
    { label: "Containers", value: SearchType.Items },
    { label: "Experiments", value: SearchType.Experiments },
    { label: "Documents", value: SearchType.Documents },
  ];

  const toggleOption = (value: string) => {
    const updated = selectedOptions.includes(value)
      ? selectedOptions.filter((v) => v !== value)
      : [...selectedOptions, value];

    setSelectedOptions(updated);

    const typedSearchTypes = updated.map(SearchTypeFromJSON);
    onSearchTypesChange(typedSearchTypes);

    if (value == "Materials" && selectedOptions.includes(value)) {
      setSelectedSubOptions([]);
      onMaterialTypesChange([]);
    }
  };

  const toggleSubOption = (value: string) => {
    const updated = selectedSubOptions.includes(value)
      ? selectedSubOptions.filter((v) => v !== value)
      : [...selectedSubOptions, value];

    setSelectedSubOptions(updated);

    const typedSearchTypes = updated.map(MaterialTypeFromJSON);
    onMaterialTypesChange(typedSearchTypes);
  };

  // Close on search dropdown menu outside click

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside)
    };
  }, []);

  return (
    <div className="dropdown" ref={dropdownRef}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="dropdown-button">
        Search ▾
      </button>

      {open && (
        <div className="dropdown-menu">
          {options.map((opt) => (
            <div key={opt.value} className="dropdown-item-wrapper">
              <label className="dropdown-item">
                <input
                  type="checkbox"
                  checked={selectedOptions.includes(opt.value)}
                  onChange={() => toggleOption(opt.value)}
                />
                {opt.label}
              </label>

              {/* Render sub-options if top-level option is selected */}
              {opt.subOptions &&
                selectedOptions.includes(opt.value) && (
                  <div className="sub-options flyout">
                    {opt.subOptions.map((sub) => (
                      <label
                        key={sub}
                        className="dropdown-sub-item"
                      >
                        <input
                          type="checkbox"
                          checked={selectedSubOptions.includes(sub)}
                          onChange={() => toggleSubOption(sub)}
                        />
                        {sub}
                      </label>
                    ))}
                  </div>
                )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
};