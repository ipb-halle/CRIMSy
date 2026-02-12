// SearchDropdown.tsx
import { useState, useRef, useEffect } from "react";
import { SearchType, MaterialType } from "../../adapters/api";
import "../../assets/css/searchDropdown.css";

interface Option {
  label: string;
  value: SearchType;
  subOptions?: MaterialType[];
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

  const [selectedOptions, setSelectedOptions] = useState<SearchType[]>([]);
  const [selectedSubOptions, setSelectedSubOptions] = useState<MaterialType[]>([]);

  const options: Option[] = [
    {
      label: "Materials",
      value: SearchType.Materials,
      subOptions: [
        MaterialType.Structure,
        MaterialType.Sequence,
        MaterialType.Biomaterial,
        MaterialType.Composition,
      ],
    },
    { label: "Items", value: SearchType.Items },
    { label: "Experiments", value: SearchType.Experiments },
    { label: "Documents", value: SearchType.Documents },
  ];

  const toggleOption = (value: SearchType) => {
    const updated = selectedOptions.includes(value)
      ? selectedOptions.filter((v) => v !== value)
      : [...selectedOptions, value];

    setSelectedOptions(updated);
    onSearchTypesChange(updated);

    // If Materials unchecked → clear sub-options
    if (value === SearchType.Materials && selectedOptions.includes(value)) {
      setSelectedSubOptions([]);
      onMaterialTypesChange([]);
    }
  };

  const toggleSubOption = (value: MaterialType) => {
    const updated = selectedSubOptions.includes(value)
      ? selectedSubOptions.filter((v) => v !== value)
      : [...selectedSubOptions, value];

    setSelectedSubOptions(updated);
    onMaterialTypesChange(updated);
  };

  // Close on outside click
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
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  return (
    <div className="dropdown" ref={dropdownRef}>
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className="dropdown-button"
      >
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

              {opt.subOptions &&
                selectedOptions.includes(opt.value) && (
                  <div className="sub-options flyout">
                    {opt.subOptions.map((sub) => (
                      <label key={sub} className="dropdown-sub-item">
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
