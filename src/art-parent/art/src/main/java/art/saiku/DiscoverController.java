/*
 * ART. A Reporting Tool.
 * Copyright (C) 2017 Enrico Liboni <eliboni@users.sf.net>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package art.saiku;

import art.servlets.Config;
import art.user.User;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.saiku.olap.discover.OlapMetaExplorer;
import org.saiku.olap.dto.SaikuConnection;
import org.saiku.olap.dto.SaikuCube;
import org.saiku.olap.dto.SaikuCubeMetadata;
import org.saiku.olap.dto.SaikuDimension;
import org.saiku.olap.dto.SaikuHierarchy;
import org.saiku.olap.dto.SaikuLevel;
import org.saiku.olap.dto.SaikuMember;
import org.saiku.olap.dto.SimpleCubeElement;
import org.saiku.olap.util.exception.SaikuOlapException;
import org.saiku.service.olap.OlapDiscoverService;
import org.saiku.service.olap.ThinQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Timothy Anyona
 */
@RestController
@RequestMapping("/saiku2/{username}/discover")
public class DiscoverController {

	private static final Logger logger = LoggerFactory.getLogger(DiscoverController.class);

	@Autowired
	private DiscoverHelper discoverHelper;

	private void createConnections(HttpSession session) throws SaikuOlapException {
		User sessionUser = (User) session.getAttribute("sessionUser");
		int userId = sessionUser.getUserId();
		Map<Integer, SaikuConnectionProvider> connections = Config.getSaikuConnections();
		Config.closeSaikuConnections(userId);

		String templatesPath = Config.getTemplatesPath();
		SaikuConnectionManager connectionManager = new SaikuConnectionManager(sessionUser, templatesPath);
		connectionManager.init();

		OlapMetaExplorer metaExplorer = new OlapMetaExplorer(connectionManager);
		OlapDiscoverService discoverService = new OlapDiscoverService(metaExplorer, connectionManager);
		ThinQueryService thinQueryService = new ThinQueryService(discoverService);

		SaikuConnectionProvider connectionProvider = new SaikuConnectionProvider();
		connectionProvider.setConnectionManager(connectionManager);
		connectionProvider.setMetaExplorer(metaExplorer);
		connectionProvider.setDiscoverService(discoverService);
		connectionProvider.setThinQueryService(thinQueryService);

		connections.put(userId, connectionProvider);
	}

	@GetMapping
	public List<SaikuConnection> discover(HttpSession session) throws SaikuOlapException {
		createConnections(session);
		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		List<SaikuConnection> connections = olapDiscoverService.getAllConnections();
		return connections;
	}

	@GetMapping("/{connection}")
	public List<SaikuConnection> getConnections(HttpSession session,
			@PathVariable("connection") String connectionName)
			throws SaikuOlapException, SQLException {

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		List<SaikuConnection> connectionList = olapDiscoverService.getConnection(connectionName);
		return connectionList;
	}

	@GetMapping("/refresh")
	public List<SaikuConnection> refreshConnections(HttpSession session) {
		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		olapDiscoverService.refreshAllConnections();
		List<SaikuConnection> connections = olapDiscoverService.getAllConnections();
		return connections;
	}

	@GetMapping("/{connection}/refresh")
	public List<SaikuConnection> refreshConnection(HttpSession session,
			@PathVariable("connection") String connectionName) {

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		olapDiscoverService.refreshConnection(connectionName);
		List<SaikuConnection> connectionList = olapDiscoverService.getConnection(connectionName);
		return connectionList;
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/metadata")
	public SaikuCubeMetadata getMetadata(HttpSession session,
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		List<SaikuDimension> dimensions = olapDiscoverService.getAllDimensions(cube);
		List<SaikuMember> measures = olapDiscoverService.getMeasures(cube);
		Map<String, Object> properties = olapDiscoverService.getProperties(cube);
		return new SaikuCubeMetadata(dimensions, measures, properties);
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/dimensions")
	public List<SaikuDimension> getDimensions(HttpSession session,
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		List<SaikuDimension> dimensions = olapDiscoverService.getAllDimensions(cube);
		return dimensions;
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/dimensions/{dimension}")
	public SaikuDimension getDimension(HttpSession session,
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName,
			@PathVariable("dimension") String dimensionName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		SaikuDimension dimension = olapDiscoverService.getDimension(cube, dimensionName);
		return dimension;
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/dimensions/{dimension}/hierarchies")
	public List<SaikuHierarchy> getDimensionHierarchies(HttpSession session,
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName,
			@PathVariable("dimension") String dimensionName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		List<SaikuHierarchy> hierarchies = olapDiscoverService.getAllDimensionHierarchies(cube, dimensionName);
		return hierarchies;
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/dimensions/{dimension}/hierarchies/{hierarchy}/levels")
	public List<SaikuLevel> getHierarchy(HttpSession session,
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName,
			@PathVariable("dimension") String dimensionName,
			@PathVariable("hierarchy") String hierarchyName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		List<SaikuLevel> levels = olapDiscoverService.getAllHierarchyLevels(cube, dimensionName, hierarchyName);
		return levels;
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/dimensions/{dimension}/hierarchies/{hierarchy}/levels/{level}")
	public List<SimpleCubeElement> getLevelMembers(HttpSession session,
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName,
			@PathVariable("dimension") String dimensionName,
			@PathVariable("hierarchy") String hierarchyName,
			@PathVariable("level") String levelName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		List<SimpleCubeElement> levelMembers = olapDiscoverService.getLevelMembers(cube, hierarchyName, levelName);
		return levelMembers;
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/hierarchies/{hierarchy}/rootmembers")
	public List<SaikuMember> getRootMembers(HttpSession session,
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName,
			@PathVariable("hierarchy") String hierarchyName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		List<SaikuMember> rootMembers = olapDiscoverService.getHierarchyRootMembers(cube, hierarchyName);
		return rootMembers;
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/hierarchies/")
	public List<SaikuHierarchy> getCubeHierarchies(HttpSession session,
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		List<SaikuHierarchy> hierarchies = olapDiscoverService.getAllHierarchies(cube);
		return hierarchies;
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/measures/")
	public List<SaikuMember> getCubeMeasures(HttpSession session,
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		List<SaikuMember> measures = olapDiscoverService.getMeasures(cube);
		return measures;
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/member/{member}")
	public SaikuMember getMember(HttpSession session,
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName,
			@PathVariable("member") String memberName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		SaikuMember member = olapDiscoverService.getMember(cube, memberName);
		return member;
	}

	@GetMapping("/{connection}/{catalog}/{schema}/{cube}/member/{member}/children")
	public List<SaikuMember> getMemberChildren(HttpSession session,
			@PathVariable("connection") String connectionName,
			@PathVariable("catalog") String catalogName,
			@PathVariable("schema") String schemaName,
			@PathVariable("cube") String cubeName,
			@PathVariable("member") String memberName) {

		if ("null".equals(schemaName)) {
			schemaName = "";
		}
		SaikuCube cube = new SaikuCube(connectionName, cubeName, cubeName, cubeName, catalogName, schemaName);

		OlapDiscoverService olapDiscoverService = discoverHelper.getDiscoverService(session);
		List<SaikuMember> children = olapDiscoverService.getMemberChildren(cube, memberName);
		return children;
	}

}
