import React, { useEffect, useState } from 'react';
import Login from './ui/pages/Login';
import Footer from './ui/components/Footer';
import Dashboard from './ui/pages/Dashboard';
import { useAuth } from "./adapters/hooks/useAuth";
import { ThemeProvider } from "./assets/css/theme/ThemeContext";
import '../src/assets/css/App.css';

const App: React.FC = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const auth = useAuth(() => {
    if (!localStorage.getItem("token")) {
      setIsLoggedIn(false);
    }
  });

  useEffect(() => {
    auth.checkSession()
      .then((isValid) => {
        setIsLoggedIn(isValid);
      })
      .catch((err) => {
        console.log("Session check failed: ", err);
        setIsLoggedIn(false);
      });
  }, []);

  return (
    <ThemeProvider>
      <div className='App'>
        <div className='content'>
          {isLoggedIn ? (
            <Dashboard
              auth={auth}
              onLogout={() => setIsLoggedIn(false)}
            />
          ) : (
            <Login
              auth={auth}
              onLogin={() => setIsLoggedIn(true)}
            />
          )}
        </div>
        <Footer />
      </div>
    </ThemeProvider>
  );
}

export default App;
