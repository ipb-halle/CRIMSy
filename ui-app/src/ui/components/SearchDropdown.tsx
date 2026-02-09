import { useState } from "react";
import "../../assets/css/searchDropdown.css"

interface Option {
  label: string;
  value: string;
  subOptions?: string[];
}

interface Props {
  onSearchTypesChange: (types: string[]) => void;
  onMaterialTypesChange: (types: string[]) => void;
}

export const SearchDropdown = ({
  onSearchTypesChange,
  onMaterialTypesChange,
}: Props) => {
  const [open, setOpen] = useState(false);
  const [selectedOptions, setSelectedOptions] = useState<string[]>([]);
  const [selectedSubOptions, setSelectedSubOptions] = useState<string[]>([]);

  const options: Option[] = [
    {
      label: "Materials",
      value: "Materials",
      subOptions: ["Strukturen", "Sequenzen", "Biomaterial", "Komposition"]
    },
    { label: "Containers", value: "Containers" },
    { label: "Experiments", value: "Experiments" },
    { label: "Documents", value: "Documents" },
  ];

  const toggleOption = (value: string) => {
    const updated = selectedOptions.includes(value)
      ? selectedOptions.filter((v) => v !== value)
      : [...selectedOptions, value];

    setSelectedOptions(updated);
    onSearchTypesChange(updated);

    if (value == "Materials" && selectedOptions.includes(value)) {
      setSelectedOptions([]);
      onSearchTypesChange([]);
    }
  };

  const toggleSubOption = (value: string) => {
    const updated = selectedOptions.includes(value)
      ? selectedOptions.filter((v) => v !== value)
      : [...selectedOptions, value];

    setSelectedOptions(updated);
    onSearchTypesChange(updated);
  };

  return (
    <div className="dropdown">
      <button
        onClick={() => setOpen(!open)}
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