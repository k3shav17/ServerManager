package com.server.resource;

import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import com.server.enumeration.Status;
import com.server.model.Response;
import com.server.model.Server;
import com.server.service.ServerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/server")
public class ServerController {

	private final ServerService serverService;

	@GetMapping("/list")
	public ResponseEntity<Response> getServers() {
		return ResponseEntity.ok(Response.builder().timeStamp(now()).data(Map.of("Servers", serverService.list(30)))
				.message("Servers retrieved").status(OK).statusCode(OK.value()).build());
	}

	@GetMapping("/ping/{ipAddress}")
	public ResponseEntity<Response> pingServer(@PathVariable("ipAddress") String ipAddress) throws IOException {
		Server server = serverService.ping(ipAddress);

		return ResponseEntity.ok(Response.builder().timeStamp(now()).data(Map.of("Server", server))
				.message(server.getStatus() == Status.SERVER_UP ? "Ping success" : "Ping failed").status(OK)
				.statusCode(OK.value()).build());
	}

	@GetMapping("/get/{id}")
	public ResponseEntity<Response> getServer(@PathVariable("id") Long id) {
		return ResponseEntity.ok(Response.builder().timeStamp(now()).data(Map.of("Server", serverService.get(id)))
				.message("Server fetched").status(OK).statusCode(OK.value()).build());
	}

	@PostMapping("/save")
	public ResponseEntity<Response> saveServer(@RequestBody @Valid Server server) {
		return ResponseEntity.ok(Response.builder().timeStamp(now())
				.data(Map.of("Server", serverService.create(server))).message("Server Created")
				.status(HttpStatus.CREATED).statusCode(HttpStatus.CREATED.value()).build());
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<Response> deleteServer(@PathVariable("id") Long id) {
		return ResponseEntity.ok(Response.builder().timeStamp(now()).data(Map.of("Deleted", serverService.delete(id)))
				.message("Server deleted").status(OK).statusCode(OK.value()).build());
	}

	@GetMapping(path = "/{fileName}", produces = IMAGE_PNG_VALUE)
	public byte[] getServerImage(@PathVariable("fileName") String fileName) throws IOException {
		return Files.readAllBytes(Paths.get(System.getProperty("user.home") + "/Downloads/" + fileName));
	}
}
