import ReactDOM from 'react-dom/client'
import './index.css'
import App from './app/App'
import { APP_NAME } from './shared/config/app'

document.title = APP_NAME

ReactDOM.createRoot(document.getElementById('root')).render(<App />)
