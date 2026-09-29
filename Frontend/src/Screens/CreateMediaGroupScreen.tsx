import React from 'react';
import {
    Modal,
    Pressable,
    ScrollView,
    StyleSheet,
    Text,
    TextInput,
    View,
} from 'react-native';
import MediaCard, { type Media } from '../Components/MediaCard';

type CreateMediaGroupScreenProps = {
    importedMedia: Media[];
    onImportPress: () => void;
    onNextPress: (groupName: string) => void;
};

export default function CreateMediaGroupScreen({
    importedMedia,
    onImportPress,
    onNextPress,
}: CreateMediaGroupScreenProps) {
    const [groupName, setGroupName] = React.useState('');
    const [isGroupNameConfirmed, setIsGroupNameConfirmed] = React.useState(false);

    return (
        <>
            <ScrollView contentContainerStyle={styles.container}>
                <Text style={styles.title}>Create Media Group</Text>
                {isGroupNameConfirmed ? (
                    <Text style={styles.groupName}>{groupName.trim()}</Text>
                ) : null}
                <Text style={styles.subtitle}>Import media to add it to your group.</Text>

                {isGroupNameConfirmed ? (
                    <Pressable style={styles.importButton} onPress={onImportPress}>
                        <Text style={styles.importButtonText}>Import Media</Text>
                    </Pressable>
                ) : null}

                {importedMedia.length > 0 ? (
                    <>
                        <View style={styles.previewSection}>
                            <Text style={styles.previewTitle}>Imported media</Text>
                            {importedMedia.map((media) => (
                                <MediaCard key={media.id} media={media} />
                            ))}
                        </View>
                        <Pressable
                            accessibilityRole="button"
                            onPress={() => onNextPress(groupName.trim())}
                            style={styles.nextButton}
                        >
                            <Text style={styles.nextButtonText}>Next</Text>
                        </Pressable>
                    </>
                ) : null}
            </ScrollView>

            <Modal
                animationType="fade"
                transparent
                visible={!isGroupNameConfirmed}
                onRequestClose={() => { }}
            >
                <View style={styles.modalOverlay}>
                    <View style={styles.modalCard}>
                        <Text style={styles.modalTitle}>Name your media group</Text>
                        <Text style={styles.modalDescription}>
                            Choose a name before importing media.
                        </Text>
                        <TextInput
                            accessibilityLabel="Media group name"
                            autoFocus
                            maxLength={25}
                            onChangeText={setGroupName}
                            onSubmitEditing={() => {
                                if (groupName.trim()) setIsGroupNameConfirmed(true);
                            }}
                            placeholder="Group name"
                            returnKeyType="done"
                            style={styles.nameInput}
                            value={groupName}
                        />
                        <Text style={styles.characterCount}>{groupName.length}/25</Text>
                        <Pressable
                            accessibilityRole="button"
                            disabled={!groupName.trim()}
                            onPress={() => setIsGroupNameConfirmed(true)}
                            style={({ pressed }) => [
                                styles.confirmButton,
                                !groupName.trim() && styles.confirmButtonDisabled,
                                pressed && groupName.trim() && styles.confirmButtonPressed,
                            ]}
                        >
                            <Text style={styles.confirmButtonText}>Next</Text>
                        </Pressable>
                    </View>
                </View>
            </Modal>
        </>
    );
}

const styles = StyleSheet.create({
    container: {
        padding: 24,
        backgroundColor: '#f4f7fb',
        flexGrow: 1,
    },
    title: {
        fontSize: 28,
        fontWeight: '700',
        marginBottom: 10,
        color: '#1f2937',
        textAlign: 'center',
    },
    subtitle: {
        fontSize: 16,
        color: '#4b5563',
        textAlign: 'center',
        marginBottom: 20,
    },
    groupName: {
        fontSize: 20,
        fontWeight: '600',
        color: '#1f2937',
        textAlign: 'center',
        marginBottom: 8,
    },
    previewSection: {
        width: '100%',
        marginTop: 12,
        marginBottom: 18,
    },
    previewTitle: {
        fontSize: 18,
        fontWeight: '700',
        color: '#1f2937',
        marginBottom: 12,
    },
    importButton: {
        alignItems: 'center',
        backgroundColor: '#2f80ed',
        borderRadius: 12,
        paddingVertical: 12,
        paddingHorizontal: 20,
    },
    importButtonText: {
        color: '#ffffff',
        fontWeight: '700',
        fontSize: 16,
    },
    nextButton: {
        alignItems: 'center',
        backgroundColor: '#1f9d8a',
        borderRadius: 12,
        marginTop: 4,
        paddingVertical: 14,
        paddingHorizontal: 20,
    },
    nextButtonText: {
        color: '#ffffff',
        fontWeight: '700',
        fontSize: 16,
    },
    modalOverlay: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 24,
        backgroundColor: 'rgba(17, 24, 39, 0.45)',
    },
    modalCard: {
        width: '100%',
        maxWidth: 380,
        padding: 24,
        borderRadius: 18,
        backgroundColor: '#ffffff',
    },
    modalTitle: {
        color: '#1f2937',
        fontSize: 22,
        fontWeight: '700',
        marginBottom: 8,
    },
    modalDescription: {
        color: '#4b5563',
        fontSize: 15,
        marginBottom: 18,
    },
    nameInput: {
        borderWidth: 1,
        borderColor: '#cbd5e1',
        borderRadius: 10,
        paddingHorizontal: 14,
        paddingVertical: 12,
        fontSize: 16,
        color: '#111827',
    },
    characterCount: {
        alignSelf: 'flex-end',
        color: '#64748b',
        fontSize: 12,
        marginTop: 6,
        marginBottom: 18,
    },
    confirmButton: {
        alignItems: 'center',
        backgroundColor: '#2f80ed',
        borderRadius: 10,
        paddingVertical: 13,
    },
    confirmButtonDisabled: {
        backgroundColor: '#a8bfdc',
    },
    confirmButtonPressed: {
        opacity: 0.85,
    },
    confirmButtonText: {
        color: '#ffffff',
        fontSize: 16,
        fontWeight: '700',
    },
});