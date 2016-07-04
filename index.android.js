'use strict';

import React, {
  Component,
} from 'react';
import {
  Text,
  View,
  StyleSheet,
  AppRegistry,
} from 'react-native';

class Nina extends Component {
  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.hello}>Hello, World</Text>
      </View>
    )
  }
}

var styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
  },
  hello: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
});

AppRegistry.registerComponent('Nina', () => Nina);
