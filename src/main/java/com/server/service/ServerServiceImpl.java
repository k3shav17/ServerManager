package com.server.service;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Random;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.server.enumeration.Status;
import com.server.model.Server;
import com.server.repository.ServerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ServerServiceImpl implements ServerService {

	private ServerRepository serverRepository;

	@Override
	public Server create(Server server) {
		log.info("Creating a new server instance : {}", server.getName());
		server.setImageURL(setServerImagerURL());
		return serverRepository.save(server);
	}

	@Override
	public List<Server> list(int limit) {
		log.info("Fetching servers ");
		return serverRepository.findAll(PageRequest.of(0, limit)).toList();
	}

	@Override
	public Server get(Long id) {
		log.info("Fetching server by ID : {}", id);
		return serverRepository.findById(id).get();
	}

	@Override
	public Server update(Server server) {
		log.info("Updating server instance : {}", server.getName());
		return serverRepository.save(server);
	}

	@Override
	public Boolean delete(Long id) {
		log.info("Deleting server by ID : {}", id);
		serverRepository.deleteById(id);
		return true;
	}

	@Override
	public Server ping(String ipAddress) throws IOException {

		log.info("Pinging server IP: {}", ipAddress);
		Server server = serverRepository.findByIpAddress(ipAddress);
		InetAddress address = InetAddress.getByName(ipAddress);
		server.setStatus(address.isReachable(10000) ? Status.SERVER_UP : Status.SERVER_DOWN);
		serverRepository.save(server);
		return server;
	}

	private String setServerImagerURL() {

		String[] images = { "server1.png", "server2.png", "server3.png" };
		return ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/server/image/" + images[new Random().nextInt(3)]).toUriString();
	}
}
