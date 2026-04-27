import React, { useEffect, useState } from 'react';
import Login from './ui/pages/Login';
import Footer from './ui/components/Footer';
import Dashboard from './ui/pages/Dashboard';
import { useAuth } from "./adapters/hooks/useAuth";
import { ThemeProvider } from "./assets/css/theme/ThemeContext";
import '../src/assets/css/App.css';

const App: React.FC = () => {
  //const [isLoggedIn, setIsLoggedIn] = useState(false);

  const auth = useAuth();
  const isLoggedIn = !!auth.roleInfo;

  return (
    <ThemeProvider>
      <div className='App'>
        <div className='content'>
          {isLoggedIn ? (
            <Dashboard
              auth={auth}
              onLogout={() => auth.handleLogout()}
            />
          ) : (
            <Login
              auth={auth}
              onLogin={() => { }}
            />
          )}
        </div>
        <Footer />
      </div>
    </ThemeProvider>
  );
}

export default App;
