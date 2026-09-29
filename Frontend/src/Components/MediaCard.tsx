import React from 'react';
import { Image, StyleSheet, Text, TouchableOpacity, View } from 'react-native';

// Type for the media
export type Media = {
    id: string;
    url: string;
    type: 'IMAGE' | 'VIDEO';
    authorName: string;
    createdAt: string;
    likes?: number;
};

type Props = {
    media: Media;
    onPress?: () => void;
};

export default function MediaCard({ media, onPress }: Props) {
    return (
        <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.8}>
            <Image
                source={{ uri: media.url }}
                style={styles.image}
                resizeMode="cover"
            />

            {/* Bottom info bar */}
            <View style={styles.info}>
                <View>
                    <Text style={styles.author}>{media.authorName}</Text>
                    <Text style={styles.date}>{new Date(media.createdAt).toLocaleDateString()}</Text>
                </View>
                <View style={styles.badge}>
                    <Text style={styles.badgeText}>{media.type}</Text>
                </View>
            </View>
        </TouchableOpacity>
    );
}

const styles = StyleSheet.create({
    card: {
        backgroundColor: '#fff',
        borderRadius: 18,
        overflow: 'hidden',
        marginBottom: 16,
        elevation: 3,
        shadowColor: '#000',
        shadowOffset: { width: 0, height: 2 },
        shadowOpacity: 0.1,
        shadowRadius: 4,
    },
    image: {
        width: '100%',
        height: 280,
        backgroundColor: '#f2f2f2',
        borderTopLeftRadius: 18,
        borderTopRightRadius: 18,
    },
    info: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: 12,
    },
    author: { fontWeight: '600', fontSize: 14 },
    date: { color: '#888', fontSize: 12, marginTop: 2 },
    badge: {
        backgroundColor: '#000',
        paddingHorizontal: 8,
        paddingVertical: 4,
        borderRadius: 6,
    },
    badgeText: { color: '#fff', fontSize: 10, fontWeight: 'bold' },
});