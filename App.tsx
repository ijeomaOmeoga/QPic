// import { StatusBar } from 'expo-status-bar';
// import { Pressable, StyleSheet, Text, View } from 'react-native';

// export default function App() {
//   return (
//     <View style={styles.container}>
//       <Text>Open up App.tsx to start working on your app!</Text>
//       <StatusBar style="auto" />
//     </View>
//   );
// }

// const styles = StyleSheet.create({
//   container: {
//     flex: 1,
//     backgroundColor: '#ee08db',
//     alignItems: 'center',
//     justifyContent: 'center',
//   },
// });





import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import {
  createNativeStackNavigator,
  type NativeStackNavigationProp,
} from '@react-navigation/native-stack';
import { useNavigation } from '@react-navigation/native';
import Ionicons from 'react-native-vector-icons/Ionicons';
import Homepage from './Frontend/src/Screens/Homepage';
import CreateMediaGroupScreen from './Frontend/src/Screens/CreateMediaGroupScreen';
import MediaImport from './Frontend/src/Screens/MediaImport';
import QRGenerator from './Frontend/src/Screens/QRGenerator';
import LoginScreen from './Frontend/src/Screens/LoginScreen';
import QRScanner from './Frontend/src/Components/QRScanner';
import type { Media } from './Frontend/src/Components/MediaCard';

type RootTabParamList = {
  Home: undefined;
  Settings: undefined;
};

type RootStackParamList = {
  MainTabs: undefined;
  Login: undefined;
  CreateMediaGroup: undefined;
  MediaImport: undefined;
  QRGenerator: undefined;
  QRScanner: undefined;
};

const Tab = createBottomTabNavigator<RootTabParamList>();
const Stack = createNativeStackNavigator<RootStackParamList>();

function HomeScreen() {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

  return (
    <Homepage
      onGeneratePress={() => navigation.navigate('CreateMediaGroup')}
      onScanPress={() => navigation.navigate('QRScanner')}
    />
  );
}

function SettingsScreen() {
  const navigation = useNavigation<NativeStackNavigationProp<RootStackParamList>>();

  return (
    <View style={styles.center}>
      <Text style={styles.text}>Settings Screen</Text>
      <Pressable style={styles.settingsButton} onPress={() => navigation.navigate('Login')}>
        <Text style={styles.settingsButtonText}>Create Account</Text>
      </Pressable>
    </View>
  );
}

function MainTabs() {
  return (
    <Tab.Navigator
      screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName = '';

          if (route.name === 'Home') {
            iconName = focused ? 'home' : 'home-outline';
          } else if (route.name === 'Settings') {
            iconName = focused ? 'settings' : 'settings-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#8448bd',
        tabBarInactiveTintColor: 'gray',
        tabBarStyle: { height: 60, paddingBottom: 8 },
      })}
    >
      <Tab.Screen name="Home" component={HomeScreen} />
      <Tab.Screen name="Settings" component={SettingsScreen} />
    </Tab.Navigator>
  );
}

export default function App() {
  const [importedMedia, setImportedMedia] = React.useState<Media[]>([]);
  const [mediaGroupName, setMediaGroupName] = React.useState('');

  return (
    <NavigationContainer>
      <Stack.Navigator initialRouteName="MainTabs" screenOptions={{ headerTintColor: '#8448bd' }}>
        <Stack.Screen
          name="MainTabs"
          component={MainTabs}
          options={{ headerShown: false }}
        />
        <Stack.Screen name="Login" component={LoginScreen} options={{ title: 'Create Account' }} />
        <Stack.Screen name="CreateMediaGroup" options={{ title: '' }}>
          {({ navigation }) => (
            <CreateMediaGroupScreen
              importedMedia={importedMedia}
              onImportPress={() => navigation.navigate('MediaImport')}
              onNextPress={(name) => {
                setMediaGroupName(name);
                navigation.navigate('QRGenerator');
              }}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="MediaImport" options={{ title: '' }}>
          {({ navigation }) => (
            <MediaImport
              onCancel={() => navigation.goBack()}
              onMediaSelected={(media) => {
                setImportedMedia((currentMedia) => [...currentMedia, ...media]);
                navigation.goBack();
              }}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="QRGenerator" options={{ title: 'QR Generator' }}>
          {({ navigation }) => (
            <QRGenerator
              groupName={mediaGroupName}
              importedMedia={importedMedia}
              onBack={() => navigation.goBack()}
            />
          )}
        </Stack.Screen>
        <Stack.Screen name="QRScanner" options={{ title: 'Scan QRCode' }}>
          {({ navigation }) => (
            <QRScanner
              onScanned={(groupId) => {
                console.log('Scanned group id:', groupId);
                navigation.navigate('MainTabs');
              }}
              onBack={() => navigation.goBack()}
            />
          )}
        </Stack.Screen>
      </Stack.Navigator>
    </NavigationContainer>
  );
}

const styles = StyleSheet.create({
  center: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#f5f5f5',
  },
  text: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  settingsButton: {
    marginTop: 16,
    paddingHorizontal: 20,
    paddingVertical: 12,
    borderRadius: 10,
    backgroundColor: '#8448bd',
  },
  settingsButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '600',
  },
});
