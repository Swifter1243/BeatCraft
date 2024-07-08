// point of this script is to avoid me tediously entering color schemes for each environment
// I do this by parsing the BSMG wiki markdown. it's really scuffed :)

const fs = require('fs')

const url = 'https://raw.githubusercontent.com/bsmg/wiki/master/wiki/mapping/lighting-defaults.md';

const switchesOutput = "output/color_schemes_switch_statement.txt"
const functionsOutput = "output/color_schemes_functions.txt"

fetch(url)
    .then(response => response.text())
    .then(useContent)
    .catch(error => console.error('Error loading text content:', error));

const switches = []
const functions = []

function useContent(text) {
    const start = text.indexOf("### The First")
    const end = text.indexOf("### Glass Desert")
    const content = text.substring(start, end)

    // split the text into different color schemes
    const colorSchemes = content.split(/^### /gm).filter(scheme => scheme.trim() !== '')
    colorSchemes.forEach(parseColorScheme)

    serialize()
}

const environmentNames = {
    "The First (Default)": [
        'DefaultEnvironment',
        'TriangleEnvironment',
        'NiceEnvironment',
        'BigMirrorEnvironment',
        'DragonsEnvironment',
        'MonstercatEnvironment',
        'PanicEnvironment'
    ],
    "Origins": ['OriginsEnvironment'],
    "KDA": ['KDAEnvironment'],
    "Crab Rave": ['CrabRaveEnvironment'],
    "Rocket": ['RocketEnvironment'],
    "Green Day": ['GreenDayEnvironment'],
    "Timbaland": ['TimbalandEnvironment'],
    "FitBeat": ['FitBeatEnvironment'],
    "Linkin Park": ['LinkinParkEnvironment'],
    "BTS": ['BTSEnvironment'],
    "Kaleidoscope": ['KaleidoscopeEnvironment'],
    "Interscope": ['InterscopeEnvironment'],
    "Skrillex": ['SkrillexEnvironment'],
    "Billie Eilish": ['BillieEnvironment'],
    "Spooky": ['HalloweenEnvironment'],
    "Lady Gaga": ['GagaEnvironment'],
    "Noir": [],
    "Default Custom": []
}

function parseColorScheme(schemeText) {
    const parts = schemeText.split(/^#### /gm).map(part => part.trim());

    const title = parts[0].split("\n")[0].trim()
    const environments = environmentNames[title]
    const colorLines = parts.splice(1).map(parseColorPart)
    const colorLinesFormatted = colorLines.join("\n    ")

    environments.forEach(environment => addColorScheme(environment, colorLinesFormatted))
}

function addColorScheme(environment, colorLines) {
    const functionName = `get${environment}()`
    const fn =
`public static ColorScheme ${functionName} {
    ColorScheme colorScheme = new ColorScheme();
    ${colorLines}
    return colorScheme;
}`
    functions.push(fn)

    const switchStatement = `case "${environment}" -> ${functionName};`
    switches.push(switchStatement)
}

const colorProperties = {
    "_colorLeft": "noteLeftColor",
    "_colorRight": "noteRightColor",
    "_envColorLeft": "environmentLeftColor",
    "_envColorLeftBoost": "environmentLeftColorBoost",
    "_envColorRight": "environmentRightColor",
    "_envColorRightBoost": "environmentRightColorBoost",
    "_obstacleColor": "obstacleColor"
}

function sanitizeJsonString(jsonString) {
    // Remove trailing commas before closing braces and brackets
    let sanitizedString = jsonString.replace(/,\s*([}\]])/g, '$1');

    // Remove trailing commas at the end of the string
    sanitizedString = sanitizedString.replace(/,\s*$/, '');

    return sanitizedString;
}

function parseColorPart(part) {
    const dirtyColorJson = part.split("```")[1].split("json")[1].trim()
    const sanitizedColorJson = `{${sanitizeJsonString(dirtyColorJson)}}`
    const colorJson = JSON.parse(sanitizedColorJson)
    const property = colorProperties[Object.keys(colorJson)[0]]
    const color = Object.values(colorJson)[0]

    const line = `colorScheme.${property} = new Color(${color.r}f, ${color.g}f, ${color.b}f);`
    return line
}

function serialize() {
    const functionsContent = functions.join("\n\n")
    fs.writeFileSync(functionsOutput, functionsContent)

    const switchContent = switches.join("\n")
    fs.writeFileSync(switchesOutput, switchContent)
}