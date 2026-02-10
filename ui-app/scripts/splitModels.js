const fs = require('fs');
const path = require('path');

const generatedDir = path.resolve(__dirname, '../src/adapters/api/generated');
const apiFile = path.join(generatedDir, 'api.ts');
const modelsDir = path.join(generatedDir, 'models');

if (!fs.existsSync(apiFile)) {
    console.error('api.ts not found! Run the generator first!');
    process.exit(1);
}

if (!fs.existsSync(modelsDir)) fs.mkdirSync(modelsDir);

const content = fs.readFileSync(apiFile, 'utf8');

const regex = /(export (interface|type) (\w+) [\s\S]*?\n\})/g;
let match;
const generatedModels = [];

while ((match = regex.exec(content)) !== null) {
    const modelContent = match[1];
    const modelName = match[3];

    fs.writeFileSync(path.join(modelsDir, `${modelName}.ts`), modelContent);
    generatedModels.push(modelName);
}

let newApiContent = content.replace(regex, '');
fs.writeFileSync(apiFile, newApiContent);

console.log(`✅ Split ${generatedModels.lenght} models onto seperate files under models/`);