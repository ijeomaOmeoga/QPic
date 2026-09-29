import React, { useState } from 'react';
import { StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';

type Props = {
  onScanned: (groupId: string) => void;
  onBack?: () => void;
};

export default function QRScanner({ onScanned, onBack }: Props) {
  const [code, setCode] = useState('');

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Join Album</Text>
      <Text style={styles.subtitle}>
        Camera disabled for development.{'\n'}
        Paste the QR code value or Group ID here.
      </Text>

      <TextInput
        style={styles.input}
        placeholder="e.g. album_123 or group_abc"
        value={code}
        onChangeText={setCode}
        autoCapitalize="none"
      />

      <TouchableOpacity
        style={[styles.button, !code && styles.buttonDisabled]}
        disabled={!code}
        onPress={() => onScanned(code.trim())}
      >
        <Text style={styles.buttonText}>Join</Text>
      </TouchableOpacity>

      {onBack ? (
        <TouchableOpacity style={styles.backButton} onPress={onBack}>
          <Text style={styles.backButtonText}>Back to Home</Text>
        </TouchableOpacity>
      ) : null}

      {/* Quick mock buttons for testing without QR */}
      <View style={styles.mockContainer}>
        <Text style={styles.mockTitle}>Quick test:</Text>
        <TouchableOpacity style={styles.mockButton} onPress={() => onScanned('album_birthday')}>
          <Text style={styles.mockText}>Simulate Scan: Birthday Party</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.mockButton} onPress={() => onScanned('album_trip')}>
          <Text style={styles.mockText}>Simulate Scan: My Trip</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, padding: 24, justifyContent: 'center', backgroundColor: '#fff' },
  title: { fontSize: 24, fontWeight: 'bold', marginBottom: 8, textAlign: 'center' },
  subtitle: { fontSize: 14, color: '#666', textAlign: 'center', marginBottom: 24 },
  input: {
    borderWidth: 1,
    borderColor: '#ddd',
    borderRadius: 12,
    padding: 16,
    fontSize: 16,
    marginBottom: 16,
  },
  button: {
    backgroundColor: '#000',
    padding: 16,
    borderRadius: 12,
    alignItems: 'center',
  },
  buttonDisabled: { backgroundColor: '#ccc' },
  buttonText: { color: '#fff', fontWeight: 'bold', fontSize: 16 },
  backButton: {
    marginTop: 12,
    paddingVertical: 12,
    borderRadius: 12,
    backgroundColor: '#2f80ed',
    alignItems: 'center',
  },
  backButtonText: { color: '#fff', fontWeight: '600', fontSize: 15 },
  mockContainer: { marginTop: 40, borderTopWidth: 1, borderTopColor: '#eee', paddingTop: 20 },
  mockTitle: { fontSize: 14, fontWeight: '600', marginBottom: 10 },
  mockButton: { backgroundColor: '#f2f2f2', padding: 12, borderRadius: 8, marginBottom: 8 },
  mockText: { color: '#333' },
});