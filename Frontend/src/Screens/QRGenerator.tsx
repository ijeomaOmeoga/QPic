import React from 'react';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import Ionicons from 'react-native-vector-icons/Ionicons';
import MediaCard, { type Media } from '../Components/MediaCard';

type QRGeneratorProps = {
    groupName: string;
    importedMedia: Media[];
    onBack: () => void;
};

export default function QRGenerator({ groupName, importedMedia, onBack }: QRGeneratorProps) {
    const [isMediaVisible, setIsMediaVisible] = React.useState(false);

    return (
        <ScrollView contentContainerStyle={styles.container}>
            <View style={styles.codePlaceholder}>
                <Ionicons name="qr-code-outline" size={112} color="#1f2937" />
            </View>
            <Text style={styles.title}>{groupName}</Text>
            <Text style={styles.summary}>
                {importedMedia.length} {importedMedia.length === 1 ? 'item' : 'items'} in this media group
            </Text>
            <Pressable
                accessibilityRole="button"
                accessibilityState={{ expanded: isMediaVisible }}
                onPress={() => setIsMediaVisible((visible) => !visible)}
                style={styles.viewMediaButton}
            >
                <Text style={styles.viewMediaButtonText}>
                    {isMediaVisible ? 'Hide Media' : 'View Media'}
                </Text>
            </Pressable>
            {isMediaVisible ? (
                <View style={styles.mediaList}>
                    {importedMedia.map((media) => (
                        <MediaCard key={media.id} media={media} />
                    ))}
                </View>
            ) : null}
            <Pressable accessibilityRole="button" onPress={onBack} style={styles.backButton}>
                <Text style={styles.backButtonText}>Back to media group</Text>
            </Pressable>
        </ScrollView>
    );
}

const styles = StyleSheet.create({
    container: {
        alignItems: 'center',
        padding: 24,
        backgroundColor: '#f4f7fb',
        flexGrow: 1,
        justifyContent: 'center',
    },
    codePlaceholder: {
        width: 200,
        height: 200,
        alignItems: 'center',
        justifyContent: 'center',
        borderRadius: 20,
        marginBottom: 24,
        backgroundColor: '#ffffff',
        borderWidth: 1,
        borderColor: '#dbe3ec',
    },
    title: {
        color: '#1f2937',
        fontSize: 24,
        fontWeight: '700',
        textAlign: 'center',
    },
    summary: {
        color: '#4b5563',
        fontSize: 16,
        marginTop: 8,
        marginBottom: 24,
    },
    viewMediaButton: {
        alignItems: 'center',
        borderRadius: 12,
        backgroundColor: '#1f9d8a',
        paddingVertical: 13,
        paddingHorizontal: 20,
    },
    viewMediaButtonText: {
        color: '#ffffff',
        fontSize: 16,
        fontWeight: '700',
    },
    mediaList: {
        alignSelf: 'stretch',
        marginTop: 20,
    },
    backButton: {
        alignItems: 'center',
        borderRadius: 12,
        backgroundColor: '#2f80ed',
        marginTop: 20,
        paddingVertical: 13,
        paddingHorizontal: 20,
    },
    backButtonText: {
        color: '#ffffff',
        fontSize: 16,
        fontWeight: '700',
    },
});
