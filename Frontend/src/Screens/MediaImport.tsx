import React from 'react';
import {
    Alert,
    Pressable,
    StyleSheet,
    Text,
    View,
} from 'react-native';
import * as DocumentPicker from 'expo-document-picker';
import AsyncStorage from '@react-native-async-storage/async-storage';
import type { Media } from '../Components/MediaCard';

type MediaImportProps = {
    onCancel?: () => void;
    onMediaSelected?: (media: Media[]) => void;
};

const STORAGE_KEY = 'media_import_permission_dismissed';

export default function MediaImport({ onCancel, onMediaSelected }: MediaImportProps) {
    const [showPermissionPrompt, setShowPermissionPrompt] = React.useState(true);
    const [doNotAskAgain, setDoNotAskAgain] = React.useState(false);
    const [isPermissionGranted, setIsPermissionGranted] = React.useState(false);

    React.useEffect(() => {
        const loadPreference = async () => {
            const value = await AsyncStorage.getItem(STORAGE_KEY);
            if (value === 'true') {
                setDoNotAskAgain(true);
                setShowPermissionPrompt(false);
                setIsPermissionGranted(true);
            }
        };

        loadPreference();
    }, []);

    const savePreference = async (value: boolean) => {
        await AsyncStorage.setItem(STORAGE_KEY, String(value));
    };

    const handlePermissionDecision = async (allow: boolean) => {
        if (doNotAskAgain && allow) {
            await savePreference(true);
        }

        if (allow) {
            setIsPermissionGranted(true);
            setShowPermissionPrompt(false);
            await openMediaPicker();
            return;
        }

        if (doNotAskAgain) {
            await savePreference(true);
        }

        setShowPermissionPrompt(false);
        Alert.alert('Permission denied', 'Please accept Permission to import media from your device.');
    };

    const openMediaPicker = async () => {
        try {
            const result = await DocumentPicker.getDocumentAsync({
                type: ['image/*', 'video/*'],
                copyToCacheDirectory: true,
                multiple: true,
            });

            if (result.canceled) {
                return;
            }

            const importedMedia: Media[] = (result.assets ?? []).map((asset, index) => ({
                id: `media-${Date.now()}-${index}`,
                url: asset.uri,
                type: asset.mimeType?.startsWith('video') ? 'VIDEO' : 'IMAGE',
                authorName: 'You',
                createdAt: new Date().toISOString(),
            }));

            onMediaSelected?.(importedMedia);

            Alert.alert(
                'Media selected',
                `${importedMedia.length} item(s) ready to import.`
            );
        } catch (error) {
            Alert.alert('Unable to import media', 'Please try again.');
        }
    };

    if (isPermissionGranted && !showPermissionPrompt) {
        return (
            <View style={styles.container}>
                <Text style={styles.title}>Media Import</Text>
                <Text style={styles.message}>Local media access is ready.</Text>
                <Pressable style={styles.primaryButton} onPress={openMediaPicker}>
                    <Text style={styles.primaryText}>Choose Media</Text>
                </Pressable>
                {onCancel ? (
                    <Pressable style={styles.secondaryButton} onPress={onCancel}>
                        <Text style={styles.secondaryText}>Cancel</Text>
                    </Pressable>
                ) : null}
            </View>
        );
    }

    return (
        <View style={styles.modalContainer}>
            <View style={styles.permissionCard}>
                <Text style={styles.title}>Allow access to local media?</Text>
                <Text style={styles.message}>
                    This lets the app import photos and videos from your device.
                </Text>


                <View style={styles.buttonRow}>
                    <Pressable
                        style={[styles.primaryButton, styles.allowButton]}
                        onPress={() => handlePermissionDecision(true)}
                    >
                        <Text style={styles.primaryText}>Allow</Text>
                    </Pressable>

                    <Pressable
                        style={[styles.secondaryButton, styles.denyButton]}
                        onPress={() => handlePermissionDecision(false)}
                    >
                        <Text style={styles.secondaryText}>Deny</Text>
                    </Pressable>
                </View>

                <Pressable
                    style={styles.checkboxRow}
                    onPress={() => setDoNotAskAgain((current) => !current)}
                >
                    <View style={[styles.checkboxBox, doNotAskAgain && styles.checkboxBoxChecked]}>
                        {doNotAskAgain ? <Text style={styles.checkmark}>✓</Text> : null}
                    </View>
                    <Text style={styles.checkboxLabel}>Do not ask again</Text>
                </Pressable>

            </View>
        </View>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 24,
        backgroundColor: '#f4f7fb',
    },
    modalContainer: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 24,
        backgroundColor: 'rgba(17,24,39,0.35)',
    },
    permissionCard: {
        width: '100%',
        maxWidth: 360,
        backgroundColor: '#ffffff',
        borderRadius: 20,
        padding: 24,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 6 },
        shadowOpacity: 0.15,
        shadowRadius: 12,
        elevation: 6,
    },
    title: {
        fontSize: 24,
        fontWeight: '700',
        marginBottom: 8,
        color: '#111827',
    },
    message: {
        fontSize: 16,
        color: '#4b5563',
        lineHeight: 22,
        marginBottom: 18,
    },
    checkboxRow: {
        flexDirection: 'row',
        alignItems: 'center',
        marginTop: 16,
        marginBottom: 8,
    },
    checkboxBox: {
        width: 18,
        height: 18,
        borderRadius: 5,
        borderWidth: 2,
        borderColor: '#2f80ed',
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#ffffff',
    },
    checkboxBoxChecked: {
        backgroundColor: '#2f80ed',
    },
    checkmark: {
        color: '#ffffff',
        fontSize: 12,
        fontWeight: '700',
        lineHeight: 16,
    },
    checkboxLabel: {
        marginLeft: 10,
        fontSize: 15,
        color: '#1f2937',
    },
    buttonRow: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        gap: 12,
    },
    primaryButton: {
        flex: 1,
        backgroundColor: '#2f80ed',
        borderRadius: 12,
        paddingVertical: 12,
        alignItems: 'center',
    },
    secondaryButton: {
        flex: 1,
        backgroundColor: '#e5e7eb',
        borderRadius: 12,
        paddingVertical: 12,
        alignItems: 'center',
    },
    allowButton: {
        backgroundColor: '#2f80ed',
    },
    denyButton: {
        backgroundColor: '#d1d5db',
    },
    primaryText: {
        color: '#ffffff',
        fontWeight: '700',
        fontSize: 16,
    },
    secondaryText: {
        color: '#111827',
        fontWeight: '700',
        fontSize: 16,
    },
});
