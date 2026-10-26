import { useEffect, useState } from 'react'
import './App.css'

// Backend URL. Can be overridden with VITE_API_URL; otherwise uses the Railway URL.
const API_URL =
  import.meta.env.VITE_API_URL || 'https://taskflow-m4-production.up.railway.app'

function App() {
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Runs once when the page opens: fetch tasks from the API
  useEffect(() => {
    fetch(`${API_URL}/api/tasks`)
      .then((response) => {
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`)
        }
        return response.json()
      })
      .then((data) => setTasks(data))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  return (
    <main className="container">
      <h1>TaskFlow</h1>
      <p className="subtitle">Tasks loaded from {API_URL}/api/tasks</p>

      {loading && <p>Loading tasks…</p>}

      {error && (
        <p className="error">
          Could not load tasks: {error}. Open the browser console for details.
        </p>
      )}

      {!loading && !error && tasks.length === 0 && <p>No tasks yet.</p>}

      <ul className="task-list">
        {tasks.map((task) => (
          <li key={task.id} className="task">
            <span className={task.completed ? 'status done' : 'status pending'}>
              {task.completed ? 'Done' : 'Pending'}
            </span>
            <div>
              <strong>{task.title}</strong>
              {task.description && <p>{task.description}</p>}
            </div>
          </li>
        ))}
      </ul>
    </main>
  )
}

export default App