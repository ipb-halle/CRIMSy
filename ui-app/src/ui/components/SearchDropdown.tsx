import { useState } from "react";
import "../../assets/css/searchDropdown.css"

interface Option {
  label: string;
  value: string;
  subOptions?: string[];
}

export const SearchDropdown = () => {
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
    setSelectedOptions((prev) =>
      prev.includes(value)
        ? prev.filter((v) => v !== value)
        : [...prev, value]
    );
    alert(`You selected ${value}`);
  };


  const toggleSubOption = (value: string) => {
    setSelectedSubOptions((prev) =>
      prev.includes(value) ? prev.filter((v) => v !== value) : [...prev, value]
    );
    alert(`You selected ${value}`);
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
              <label
                className={`dropdown-item ${selectedOptions.includes(opt.value) ? "selected" : ""
                  }`}
              >
                <input
                  type="checkbox"
                  checked={selectedOptions.includes(opt.value)}
                  onChange={() => toggleOption(opt.value)}
                />
                {opt.label}
              </label>

              {/* Render sub-options if top-level option is selected */}
              {opt.subOptions && selectedOptions.includes(opt.value) && (
                <div className="sub-options flyout">
                  {opt.subOptions.map((subOption) => (
                    <label
                      key={subOption}
                      className={`dropdown-sub-item ${selectedSubOptions.includes(subOption) ? "selected" : ""
                        }`}
                    >
                      <input
                        type="checkbox"
                        checked={selectedSubOptions.includes(subOption)}
                        onChange={() => toggleSubOption(subOption)}
                      />
                      {subOption}
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

