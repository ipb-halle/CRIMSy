import React, { useEffect, useState } from 'react';
import '../src/assets/css/App.css';

import Login from './ui/pages/Login';
import HeadMeta from './ui/components/HeadMeta';
import Footer from './ui/components/Footer';
import Growl from './ui/components/Growl';
import Dashboard from './ui/pages/Dashboard';
import { useAuth } from "./adapters/hooks/useAuth";
import { ThemeProvider } from "./ui/theme/ThemeContext";

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
        <HeadMeta />
        <div className='content'>
          {isLoggedIn ? (
            <Dashboard onLogout={() => setIsLoggedIn(false)} />
          ) : (
            <Login onLogin={() => setIsLoggedIn(true)} />
          )}
          <Growl />
        </div>
        <Footer />
      </div>
    </ThemeProvider>
  );
}

export default App;
