import React from 'react';
import './App.css';

import Login from './ui/pages/Login';
import HeadMeta from './ui/components/HeadMeta';
import Footer from './ui/components/Footer';
import Growl from './ui/components/Growl';

const App: React.FC = () => {
  return (
    <div className='App'>
      <HeadMeta />
      <div className='content'>
        {/* Always render Login; it manages logged-in state */}
        <Login />
        <Growl />
      </div>
      <Footer />
    </div>
  );
}

export default App;
