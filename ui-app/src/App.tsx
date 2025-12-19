import React from 'react';
import './App.css';

import Login from './pages/Login';
import HeadMeta from './components/HeadMeta';
import Footer from './components/Footer';
import Growl from './components/Growl';

const App: React.FC = () => {
  return (


    <div className='App'>
      <HeadMeta />
      <div className='content'>
        <Login />
        <Growl />
      </div>
      <Footer />

    </div>
  );
}

export default App;