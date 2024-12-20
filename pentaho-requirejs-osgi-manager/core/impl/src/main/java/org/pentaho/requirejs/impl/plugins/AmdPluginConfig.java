/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.requirejs.impl.plugins;

import org.pentaho.requirejs.IRequireJsPackageConfiguration;
import org.pentaho.requirejs.IRequireJsPackageConfigurationPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Handles the moduleId version mapping inside configurations for the amd! loader plugin.
 */
public class AmdPluginConfig implements IRequireJsPackageConfigurationPlugin {
  @Override
  public void apply( IRequireJsPackageConfiguration requireJsPackageConfig,
                     Function<String, IRequireJsPackageConfiguration> dependencyResolver,
                     Function<String, String> resolveModuleId,
                     Map<String, ?> requireConfig ) {
    if ( requireConfig.containsKey( "config" ) ) {
      Map<String, ?> config = (Map<String, ?>) requireConfig.get( "config" );

      if ( config.containsKey( "amd" ) ) {
        convertAmdConfigurations( (Map<String, Object>) config.get( "amd" ), resolveModuleId );
      }
    }
  }

  private void convertAmdConfigurations( Map<String, Object> configuration, Function<String, String> resolveModuleId ) {
    if ( configuration.containsKey( "shim" ) ) {
      Map<String, Map<String, ?>> originalShimConfiguration = (Map<String, Map<String, ?>>) configuration.get( "shim" );
      Map<String, ?> convertAmdShimConfigurations = convertAmdShimConfigurations( originalShimConfiguration, resolveModuleId );

      configuration.put( "shim", convertAmdShimConfigurations );
    }
  }

  private Map<String, ?> convertAmdShimConfigurations( Map<String, Map<String, ?>> originalShimConfiguration, Function<String, String> resolveModuleId ) {
    HashMap<String, Object> convertedShimConfiguration = new HashMap<>();

    for ( String moduleId : originalShimConfiguration.keySet() ) {
      String versionedModuleId = resolveModuleId.apply( moduleId );

      HashMap<String, Object> shimConfig = new HashMap<>();

      Map<String, ?> originalModuleShimConfiguration = originalShimConfiguration.get( moduleId );
      for ( String key : originalModuleShimConfiguration.keySet() ) {
        Object originalValue = originalModuleShimConfiguration.get( key );
        Object convertedValue = originalValue;

        if ( key.equals( "deps" ) ) {
          HashMap<String, Object> originalDeps = (HashMap<String, Object>) originalValue;
          HashMap<String, Object> convertedDeps = new HashMap<>();

          for ( String depModuleId : originalDeps.keySet() ) {
            String versionedDepModuleId = resolveModuleId.apply( depModuleId );
            convertedDeps.put( versionedDepModuleId, originalDeps.get( depModuleId ) );
          }

          convertedValue = convertedDeps;
        }

        shimConfig.put( key, convertedValue );
      }

      convertedShimConfiguration.put( versionedModuleId, shimConfig );
    }

    return convertedShimConfiguration;
  }
}
