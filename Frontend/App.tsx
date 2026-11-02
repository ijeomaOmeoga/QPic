import React, { useState } from 'react';
import { SafeAreaView, StatusBar, StyleSheet } from 'react-native';

import type { Media } from './src/Components/MediaCard';
import QRScanner from './src/Components/QRScanner';
import CreateMediaGroupScreen from './src/Screens/CreateMediaGroupScreen';
import Homepage from './src/Screens/Homepage';
import LoginScreen from './src/Screens/LoginScreen';
import MediaImport from './src/Screens/MediaImport';
import QRGenerator from './src/Screens/QRGenerator';
import SettingsScreen from './src/Screens/SettingsScreen';

type Screen =
  | 'login'
  | 'home'
  | 'settings'
  | 'create-group'
  | 'media-import'
  | 'qr-generator'
  | 'qr-scanner';

export default function App() {
  const [screen, setScreen] = useState<Screen>('login');
  const [groupName, setGroupName] = useState('My Shared Album');
  const [importedMedia, setImportedMedia] = useState<Media[]>([]);

  const goHome = () => setScreen('home');

  const handleMediaSelected = (media: Media[]) => {
    setImportedMedia((current) => [...current, ...media]);
    setScreen('create-group');
  };

  const handleNextStep = (nextGroupName: string) => {
    const trimmedName = nextGroupName.trim() || 'Untitled Media Group';
    setGroupName(trimmedName);
    setScreen('qr-generator');
  };

  return (
    <SafeAreaView style={styles.safeArea}>
      <StatusBar barStyle="dark-content" backgroundColor="#f5f1e8" />

      {screen === 'login' && <LoginScreen onSubmit={goHome} />}

      {screen === 'home' && (
        <Homepage
          onGeneratePress={() => setScreen('create-group')}
          onScanPress={() => setScreen('qr-scanner')}
        />
      )}

      {screen === 'settings' && <SettingsScreen />}

      {screen === 'create-group' && (
        <CreateMediaGroupScreen
          importedMedia={importedMedia}
          onImportPress={() => setScreen('media-import')}
          onNextPress={handleNextStep}
        />
      )}

      {screen === 'media-import' && (
        <MediaImport
          onCancel={goHome}
          onMediaSelected={handleMediaSelected}
        />
      )}

      {screen === 'qr-generator' && (
        <QRGenerator
          groupName={groupName}
          importedMedia={importedMedia}
          onBack={() => setScreen('create-group')}
        />
      )}

      {screen === 'qr-scanner' && (
        <QRScanner
          onScanned={(scannedValue) => {
            const scannedGroupName = scannedValue.trim() || 'Scanned Album';
            setGroupName(scannedGroupName);
            setScreen('home');
          }}
          onBack={goHome}
        />
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#f5f1e8',
  },
});
