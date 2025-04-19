import datetime
import json
import re
import requests
import sqlite3

# Database connection
DB_URL = 'sudokugames.db'
conn = sqlite3.connect(DB_URL)
cursor = conn.cursor()

# Get today's date in MM/DD/YY format
today = datetime.date.today()
today = today.strftime("%m/%d/%y")

# Check if today's puzzles already exist in the database
query = "SELECT COUNT(*) FROM puzzles WHERE title = ?"
cursor.execute(query, (f"NYT {today}",))
count = cursor.fetchone()[0]

if count > 0:
    print(f"Today's puzzles (NYT {today}) already exist in the database.")
    conn.close()
    exit(0)

# URL for the New York Times Sudoku puzzles
url = 'https://www.nytimes.com/puzzles/sudoku/'
response = requests.get(url)

# Regex pattern to extract game data
pattern = r'<script type="text\/javascript">window\.gameData = (.+)<\/script><\/div><div id="portal-editorial-content">'
match = re.search(pattern, response.text)

if match:
    # Parse the JSON data
    data = json.loads(match.group(1))
    
    EasyPuzzle = data['easy']['puzzle_data']['puzzle']
    MediumPuzzle = data['medium']['puzzle_data']['puzzle']
    HardPuzzle = data['hard']['puzzle_data']['puzzle']

    EasySolution = data['easy']['puzzle_data']['solution']
    MediumSolution = data['medium']['puzzle_data']['solution']
    HardSolution = data['hard']['puzzle_data']['solution']

    Puzzles = [EasyPuzzle, MediumPuzzle, HardPuzzle]
    Solutions = [EasySolution, MediumSolution, HardSolution]

    title = f"NYT {today}"
    status = "not started"

    for i in range(len(Puzzles)):
        if i == 0:
            difficulty = "easy"
        elif i == 1:
            difficulty = "medium"
        else:
            difficulty = "hard"
        
        puzzle = Puzzles[i]
        solution = Solutions[i]

        puzzle_sdx = ''
        for j in range(len(puzzle)):
            if puzzle[j] == 0:
                puzzle_sdx += '0 '
            else:
                puzzle_sdx += 'u' + str(puzzle[j]) + ' '

        solution_sdx = ''
        for j in range(len(solution)):
            if puzzle[j] == 0:
                solution_sdx += str(solution[j]) + ' '
            else:
                solution_sdx += 'u' + str(puzzle[j]) + ' '

        puzzle_sdx = puzzle_sdx.strip()
        solution_sdx = solution_sdx.strip()

        query = "INSERT INTO puzzles (title, difficulty, status, sdx, sdx_solution) VALUES (?, ?, ?, ?, ?)"
        cursor.execute(query, (title, difficulty, status, puzzle_sdx, solution_sdx))
        print(f"Inserted: {title} {difficulty}")

    conn.commit()
    conn.close()
else:
    raise Exception("Failed to find game data.")