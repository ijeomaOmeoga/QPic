// Drop this into your Expo app (e.g. src/api/types.ts). Mirrors the backend JSON 1:1.

export type UUID = string;
export type ISODate = string;

export interface ApiError {
  timestamp: ISODate;
  status: number;
  code: string;            // e.g. VALIDATION_FAILED, NOT_FOUND, SHARE_PASSWORD_REQUIRED, SHARE_EXPIRED ...
  message: string;
  path?: string;
  fieldErrors?: Record<string, string>;
}

export interface PageResponse<T> {
  content: T[]; page: number; size: number; totalElements: number; totalPages: number; last: boolean;
}

// ---------- auth
export interface UserDto { id: UUID; email: string; displayName: string; createdAt: ISODate }
export interface AuthResponse { accessToken: string; refreshToken: string; expiresInSeconds: number; user: UserDto }

// ---------- media
export type MediaKind = 'IMAGE' | 'VIDEO';
export interface MediaView {
  id: UUID; ownerId: UUID; kind: MediaKind; contentType: string; originalFilename: string | null;
  sizeBytes: number; width: number | null; height: number | null; durationMs: number | null;
  url: string;             // presigned, expires (default 6h) -> refetch the album/media to refresh
  createdAt: ISODate;
}
export interface InitUploadRequest { filename: string; contentType: string; sizeBytes: number }
export interface InitUploadResponse { mediaId: UUID; uploadUrl: string; headers: Record<string, string>; expiresAt: ISODate }
export interface CompleteUploadRequest { width?: number; height?: number; durationMs?: number }

// ---------- album -> media group -> item -> media
export interface MediaItemView { id: UUID; position: number; caption: string | null; media: MediaView }
export interface MediaGroupView {
  id: UUID; albumId: UUID; ownerId: UUID; title: string; description: string | null; position: number; items: MediaItemView[];
}
export interface AlbumView {
  id: UUID; ownerId: UUID; title: string; description: string | null; cover: MediaView | null;
  createdAt: ISODate; updatedAt: ISODate; groups: MediaGroupView[];
}
export interface AlbumSummary {
  id: UUID; title: string; description: string | null; cover: MediaView | null;
  groupCount: number; mediaCount: number; createdAt: ISODate; updatedAt: ISODate;
}
export interface AlbumRequest { title: string; description?: string; coverMediaId?: UUID }
export interface GroupRequest { title: string; description?: string; mediaIds?: UUID[] }

// ---------- sharing / QR
export type ShareTarget = 'ALBUM' | 'MEDIA_GROUP' | 'MEDIA';
export interface CreateShareRequest {
  targetType: ShareTarget; targetId: UUID; title?: string; password?: string; expiresAt?: ISODate; maxViews?: number;
}
export interface ShareDto {
  id: UUID; token: string; targetType: ShareTarget; targetId: UUID; title: string | null;
  shareUrl: string;        // <- encode this in the QR (or just show GET /api/shares/{id}/qr)
  deepLink: string; passwordProtected: boolean; expiresAt: ISODate | null; maxViews: number | null;
  viewCount: number; revoked: boolean; createdAt: ISODate;
}
export interface ShareInfo {
  targetType: ShareTarget; title: string | null; passwordProtected: boolean;
  available: boolean; unavailableReason: 'SHARE_REVOKED' | 'SHARE_EXPIRED' | 'SHARE_EXHAUSTED' | null; expiresAt: ISODate | null;
}
export interface ShareContent {
  targetType: ShareTarget; title: string | null;
  album: AlbumView | null; group: MediaGroupView | null; media: MediaView | null; expiresAt: ISODate | null;
}

// ---------- tiny helper: token from a scanned QR (https://share.example.com/s/<token> or qrshare://s/<token>)
export const tokenFromQr = (data: string): string | null =>
  /\/s\/([A-Za-z0-9_-]{16,32})\/?$/.exec(data.trim())?.[1] ?? null;

// ---------- direct upload of a video/image (recommended flow)
//   1. POST /api/media/uploads            -> { mediaId, uploadUrl, headers }
//   2. PUT  uploadUrl  (raw file body, headers as returned)   e.g. FileSystem.uploadAsync(uploadUrl, uri, { httpMethod: 'PUT', headers, uploadType: FileSystem.FileSystemUploadType.BINARY_CONTENT })
//   3. POST /api/media/{mediaId}/complete -> MediaView
