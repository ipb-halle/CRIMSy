import React, { useEffect, useState } from 'react';
import Login from './ui/pages/Login';
import Footer from './ui/components/Footer';
import Dashboard from './ui/pages/Dashboard';
import { useAuth } from "./adapters/hooks/useAuth";
import { ThemeProvider } from "./ui/theme/ThemeContext";
import '../src/assets/css/App.css';

const App: React.FC = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const { checkSession } = useAuth();

  // On mount, verify if token exists and is valid
  useEffect(() => {
    const verifyLogin = async () => {
      const valid = await checkSession();
      if (valid) setIsLoggedIn(true);
    };
    verifyLogin();
  }, []);

  return (
    <ThemeProvider>
      <div className='App'>
        <div className='content'>
          {isLoggedIn ? (
            <Dashboard onLogout={() => setIsLoggedIn(false)} />
          ) : (
            <Login onLogin={() => setIsLoggedIn(true)} />
          )}
        </div>
        <Footer />
      </div>
    </ThemeProvider>
  );
}

export default App;
