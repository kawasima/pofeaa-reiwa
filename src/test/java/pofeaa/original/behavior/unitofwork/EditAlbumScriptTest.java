package pofeaa.original.behavior.unitofwork;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EditAlbumScriptTest {
    
    @Mock
    private MapperRegistry mapperRegistry;
    
    @Mock
    private DataMapper<Album> albumMapper;
    
    private EditAlbumScript editAlbumScript;
    private AutoCloseable mocks;
    
    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
        editAlbumScript = new EditAlbumScript(mapperRegistry);
        
        // Setup default behavior
        when(mapperRegistry.getMapper(Album.class)).thenReturn(albumMapper);
    }
    
    @AfterEach
    void tearDown() throws Exception {
        // Clean up the ThreadLocal to avoid test pollution
        try {
            UnitOfWork.getCurrent();
            UnitOfWork.setCurrent(null);
        } catch (IllegalStateException e) {
            // No current UnitOfWork, which is fine
        }
        mocks.close();
    }
    
    @Nested
    @DisplayName("UpdateTitle Tests")
    class UpdateTitleTests {
        
        @Test
        @DisplayName("Should successfully update album title")
        void shouldUpdateAlbumTitle() {
            // Given
            UUID albumId = UUID.randomUUID();
            String originalTitle = "Original Title";
            String newTitle = "New Title";
            Album album = new Album(albumId, originalTitle);
            
            when(albumMapper.find(albumId)).thenReturn(album);
            
            // When
            editAlbumScript.updateTitle(albumId, newTitle);
            
            // Then
            assertThat(album.getTitle()).isEqualTo(newTitle);
            verify(mapperRegistry, times(2)).getMapper(Album.class); // Once in EditAlbumScript, once in UnitOfWork
            verify(albumMapper).find(albumId);
            verify(albumMapper).update(album);
        }
        
        @Test
        @DisplayName("Should create new UnitOfWork for the operation")
        void shouldCreateNewUnitOfWork() {
            // Given
            UUID albumId = UUID.randomUUID();
            String newTitle = "New Title";
            Album album = new Album(albumId, "Old Title");
            
            when(albumMapper.find(albumId)).thenReturn(album);
            
            // When
            editAlbumScript.updateTitle(albumId, newTitle);
            
            // Then
            // The UnitOfWork should have been created and used
            verify(albumMapper).update(album);
        }
        
        @Test
        @DisplayName("Should handle null album gracefully")
        void shouldHandleNullAlbum() {
            // Given
            UUID albumId = UUID.randomUUID();
            when(albumMapper.find(albumId)).thenReturn(null);
            
            // When & Then
            assertThatThrownBy(() -> editAlbumScript.updateTitle(albumId, "New Title"))
                .isInstanceOf(NullPointerException.class);
        }
        
        @Test
        @DisplayName("Should register album as dirty when title is changed")
        void shouldRegisterAlbumAsDirty() {
            // Given
            UUID albumId = UUID.randomUUID();
            String newTitle = "New Title";
            Album album = spy(new Album(albumId, "Old Title"));
            
            when(albumMapper.find(albumId)).thenReturn(album);
            
            // When
            editAlbumScript.updateTitle(albumId, newTitle);
            
            // Then
            verify(album).setTitle(newTitle);
            // The setTitle method should trigger registerDirty
            verify(albumMapper).update(album);
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("Should work with multiple album updates in sequence")
        void shouldHandleMultipleUpdates() {
            // Given
            UUID albumId1 = UUID.randomUUID();
            UUID albumId2 = UUID.randomUUID();
            Album album1 = new Album(albumId1, "Title 1");
            Album album2 = new Album(albumId2, "Title 2");
            
            when(albumMapper.find(albumId1)).thenReturn(album1);
            when(albumMapper.find(albumId2)).thenReturn(album2);
            
            // When
            editAlbumScript.updateTitle(albumId1, "New Title 1");
            editAlbumScript.updateTitle(albumId2, "New Title 2");
            
            // Then
            assertThat(album1.getTitle()).isEqualTo("New Title 1");
            assertThat(album2.getTitle()).isEqualTo("New Title 2");
            verify(albumMapper, times(2)).update(any(Album.class));
        }
        
        @Test
        @DisplayName("Should handle empty title")
        void shouldHandleEmptyTitle() {
            // Given
            UUID albumId = UUID.randomUUID();
            Album album = new Album(albumId, "Original Title");
            
            when(albumMapper.find(albumId)).thenReturn(album);
            
            // When
            editAlbumScript.updateTitle(albumId, "");
            
            // Then
            assertThat(album.getTitle()).isEmpty();
            verify(albumMapper).update(album);
        }
        
        @Test
        @DisplayName("Should handle very long title")
        void shouldHandleVeryLongTitle() {
            // Given
            UUID albumId = UUID.randomUUID();
            String longTitle = "A".repeat(1000);
            Album album = new Album(albumId, "Short Title");
            
            when(albumMapper.find(albumId)).thenReturn(album);
            
            // When
            editAlbumScript.updateTitle(albumId, longTitle);
            
            // Then
            assertThat(album.getTitle()).isEqualTo(longTitle);
            verify(albumMapper).update(album);
        }
    }
    
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should propagate mapper exceptions")
        void shouldPropagateMapperExceptions() {
            // Given
            UUID albumId = UUID.randomUUID();
            when(albumMapper.find(albumId)).thenThrow(new RuntimeException("Database error"));
            
            // When & Then
            assertThatThrownBy(() -> editAlbumScript.updateTitle(albumId, "New Title"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database error");
        }
        
        @Test
        @DisplayName("Should handle mapper not found in registry")
        void shouldHandleMapperNotFound() {
            // Given
            UUID albumId = UUID.randomUUID();
            when(mapperRegistry.getMapper(Album.class))
                .thenThrow(new IllegalArgumentException("No mapper registered for class: Album"));
            
            // When & Then
            assertThatThrownBy(() -> editAlbumScript.updateTitle(albumId, "New Title"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No mapper registered");
        }
    }
}