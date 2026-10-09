import React, { useState } from 'react';
import {
	KeyboardAvoidingView,
	Platform,
	Pressable,
	ScrollView,
	StyleSheet,
	Text,
	TextInput,
	View,
} from 'react-native';

export default function LoginScreen() {
	const [username, setUsername] = useState('');
	const [email, setEmail] = useState('');
	const [password, setPassword] = useState('');
	const [submittedUsername, setSubmittedUsername] = useState('');
	const [error, setError] = useState('');

	function handleSignUp() {
		if (!username.trim() || !email.trim() || !password) {
			setError('Please complete every field.');
			return;
		}

		setError('');
		setSubmittedUsername(username.trim());
	}

	return (
		<KeyboardAvoidingView
			behavior={Platform.OS === 'ios' ? 'padding' : undefined}
			style={styles.screen}
		>
			<ScrollView
				contentContainerStyle={styles.content}
				keyboardShouldPersistTaps="handled"
			>
				<View style={styles.brandMark}>
					<Text style={styles.brandMarkText}>Q</Text>
				</View>
				<Text style={styles.eyebrow}>YOUR SHARED SPACE</Text>
				<Text style={styles.title}>Make room for{`\n`}the moments.</Text>
				<Text style={styles.subtitle}>
					Create your QPic account and start sharing memories with the people
					who matter.
				</Text>

				<View style={styles.form}>
					<Text style={styles.formTitle}>Create your account</Text>

					<Text style={styles.label}>Username</Text>
					<TextInput
						autoCapitalize="none"
						autoCorrect={false}
						placeholder="yourname"
						placeholderTextColor="#9b9b9b"
						style={styles.input}
						value={username}
						onChangeText={setUsername}
					/>

					<Text style={styles.label}>Email</Text>
					<TextInput
						autoCapitalize="none"
						autoCorrect={false}
						keyboardType="email-address"
						placeholder="you@example.com"
						placeholderTextColor="#9b9b9b"
						style={styles.input}
						value={email}
						onChangeText={setEmail}
					/>

					<Text style={styles.label}>Password</Text>
					<TextInput
						autoCapitalize="none"
						placeholder="At least 8 characters"
						placeholderTextColor="#9b9b9b"
						secureTextEntry
						style={styles.input}
						value={password}
						onChangeText={setPassword}
					/>

					{!!error && <Text style={styles.error}>{error}</Text>}

					<Pressable
						accessibilityRole="button"
						onPress={handleSignUp}
						style={({ pressed }: { pressed: boolean }) => [styles.button, pressed && styles.buttonPressed]}
					>
						<Text style={styles.buttonText}>Sign up</Text>
					</Pressable>
				</View>

				{!!submittedUsername && (
					<View style={styles.welcome}>
						<Text style={styles.welcomeLabel}>WELCOME</Text>
						<Text style={styles.welcomeText}>{submittedUsername}</Text>
					</View>
				)}
			</ScrollView>
		</KeyboardAvoidingView>
	);
}

const styles = StyleSheet.create({
	screen: {
		flex: 1,
		backgroundColor: '#f5f1e8',
	},
	content: {
		flexGrow: 1,
		padding: 28,
		paddingTop: 42,
	},
	brandMark: {
		alignItems: 'center',
		backgroundColor: '#e76f51',
		borderRadius: 18,
		height: 56,
		justifyContent: 'center',
		marginBottom: 36,
		width: 56,
	},
	brandMarkText: {
		color: '#fffaf2',
		fontSize: 30,
		fontWeight: '800',
	},
	eyebrow: {
		color: '#e76f51',
		fontSize: 12,
		fontWeight: '800',
		letterSpacing: 1.6,
		marginBottom: 12,
	},
	title: {
		color: '#252422',
		fontSize: 42,
		fontWeight: '800',
		letterSpacing: -1,
		lineHeight: 44,
		marginBottom: 16,
	},
	subtitle: {
		color: '#6c6962',
		fontSize: 16,
		lineHeight: 24,
		maxWidth: 350,
	},
	form: {
		backgroundColor: '#fffaf2',
		borderColor: '#e8dfd2',
		borderRadius: 18,
		borderWidth: 1,
		marginTop: 32,
		padding: 22,
	},
	formTitle: {
		color: '#252422',
		fontSize: 20,
		fontWeight: '700',
		marginBottom: 22,
	},
	label: {
		color: '#514d46',
		fontSize: 13,
		fontWeight: '700',
		marginBottom: 8,
	},
	input: {
		backgroundColor: '#f5f1e8',
		borderColor: '#e1d8ca',
		borderRadius: 10,
		borderWidth: 1,
		color: '#252422',
		fontSize: 16,
		height: 52,
		marginBottom: 16,
		paddingHorizontal: 15,
	},
	error: {
		color: '#b64032',
		fontSize: 13,
		marginBottom: 14,
	},
	button: {
		alignItems: 'center',
		backgroundColor: '#252422',
		borderRadius: 10,
		height: 54,
		justifyContent: 'center',
		marginTop: 4,
	},
	buttonPressed: {
		opacity: 0.78,
	},
	buttonText: {
		color: '#fffaf2',
		fontSize: 16,
		fontWeight: '800',
	},
	welcome: {
		alignItems: 'center',
		backgroundColor: '#d9edc2',
		borderRadius: 18,
		marginTop: 18,
		padding: 22,
	},
	welcomeLabel: {
		color: '#4e7041',
		fontSize: 12,
		fontWeight: '800',
		letterSpacing: 1.8,
		marginBottom: 7,
	},
	welcomeText: {
		color: '#283b23',
		fontSize: 26,
		fontWeight: '800',
	},
});
