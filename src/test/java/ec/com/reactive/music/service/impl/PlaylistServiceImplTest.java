package ec.com.reactive.music.service.impl;

import ec.com.reactive.music.domain.dto.PlaylistDTO;
import ec.com.reactive.music.domain.entities.Playlist;
import ec.com.reactive.music.domain.entities.Song;
import ec.com.reactive.music.repository.IPlaylistRepository;
import ec.com.reactive.music.repository.ISongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PlaylistServiceImplTest {

    @Mock
    IPlaylistRepository playlistRepositoryMock;
    ISongRepository songRepositoryMock;

    ModelMapper modelMapper;

    PlaylistServiceImpl playlistService;


    @BeforeEach
    void init(){
        modelMapper = new ModelMapper();
        playlistService = new PlaylistServiceImpl(playlistRepositoryMock, songRepositoryMock, modelMapper);
    }

    @Test
    @DisplayName("updatePlaylist()")
    void updatePlaylist() {
        Playlist playlistExpected = new Playlist();
        playlistExpected.setIdPlaylist("111111-1");
        playlistExpected.setName("DojoPlaylist");
        playlistExpected.setDuration(LocalTime.of(0,10,0));
        ArrayList<Song> songs = new ArrayList<>();
        songs.add(new Song());
        playlistExpected.setSongs(songs);

        var playlistEdited = playlistExpected.toBuilder()
                .name("DojoPlaylistEdited")
                .duration(LocalTime.of(0,15,0))
                .build();

        var playlistEditedDTO = modelMapper.map(playlistEdited, PlaylistDTO.class);

        ResponseEntity<PlaylistDTO> playlistDTOResponseEntity = new ResponseEntity<>(playlistEditedDTO, HttpStatus.ACCEPTED);

        //Mocking
        Mockito.when(playlistRepositoryMock.findById(Mockito.any(String.class))).thenReturn(Mono.just(playlistExpected));
        Mockito.when(playlistRepositoryMock.save(Mockito.any(Playlist.class))).thenReturn(Mono.just(playlistEdited));

        var service = playlistService.updatePlaylist("111111-1",playlistEditedDTO);

        //Assert
        StepVerifier.create(service)
                .expectNext(playlistDTOResponseEntity)
                .expectComplete()
                .verify();
        Mockito.verify(playlistRepositoryMock).save(playlistEdited);
    }
}