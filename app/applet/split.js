const fs = require('fs');
const path = 'app/src/main/java/lumia/tracker/ui/screens/SettingsScreen.kt';
const lines = fs.readFileSync(path, 'utf-8').split('\n');

let imports = [];
let i = 0;
while (i < lines.length) {
    if (lines[i].trim() === '@Composable' || lines[i].trim().startsWith('@OptIn')) {
        break;
    }
    imports.push(lines[i]);
    i++;
}

const importString = imports.join('\n');

const extractScreens = [
    'AppearanceScreen',
    'BetaFeaturesScreen',
    'DataManagementScreen',
    'SafetyFeaturesScreen',
    'AdvancedThemeScreen',
    'SystemSettingsScreen',
    'NotificationsScreen',
    'AboutAppScreen'
];

let currentScreen = 'SettingsScreen';
let files = {
    'SettingsScreen': []
};

let buffer = [];

for (let j = i; j < lines.length; j++) {
    const line = lines[j];
    
    if (line.startsWith('@Composable') || line.startsWith('@OptIn')) {
        buffer.push(line);
    } else if (line.startsWith('fun ')) {
        const funNameMatch = line.match(/^fun\ (?:<T>\ )?([A-Za-z0-9_]+)\(/);
        const funName = funNameMatch ? funNameMatch[1] : null;
        
        if (funName && extractScreens.includes(funName)) {
            currentScreen = funName;
            if (!files[currentScreen]) files[currentScreen] = [];
        } else if (funName && funName !== 'SettingsScreen') {
            currentScreen = 'SettingsScreen';
        }
        
        files[currentScreen].push(...buffer);
        files[currentScreen].push(line);
        buffer = [];
    } else {
        if (buffer.length > 0) {
            files[currentScreen].push(...buffer);
            buffer = [];
        }
        files[currentScreen].push(line);
    }
}

for (let screen in files) {
    if (screen === 'SettingsScreen') {
        fs.writeFileSync(path, importString + '\n' + files[screen].join('\n'));
    } else {
        fs.writeFileSync(`app/src/main/java/lumia/tracker/ui/screens/${screen}.kt`, importString + '\n' + files[screen].join('\n'));
    }
}
console.log("Success");
