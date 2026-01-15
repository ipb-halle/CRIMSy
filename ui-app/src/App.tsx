import React from 'react';
import './App.css';

import Login from './pages/Login';
import HeadMeta from './components/HeadMeta';
import Footer from './components/Footer';
import Growl from './components/Growl';

import RoleMessage from "./components/RoleMessage";

const App: React.FC = () => {

  const token = localStorage.getItem("token");
  return (


    <div className='App'>
      <HeadMeta />
      <div className='content'>
        {/*  <Login />*/}
        {!token ? <Login /> : <RoleMessage />}

        <Growl />
      </div>
      <Footer />

    </div>
  );
}

export default App;