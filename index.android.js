'use strict';

import React, {
  Component,
} from 'react';
import {
  Text,
  View,
  Image,
  StyleSheet,
  AppRegistry,
} from 'react-native';

class Nina extends Component {

  render() {
    return (
      <View style={styles.container}>
        <Text style={styles.hello}>Hello, World</Text>
        <Image source={{uri: "nina", isStatic: true,}}
          style={styles.image}/>
      </View>
    )
  }
}

var styles = StyleSheet.create({
  container: {
    flex: 1,
    flexDirection: 'column',
    justifyContent: 'center',
  },
  hello: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
    color: 'black',
  },
  image: {
    width: 48,
    height: 48,
    alignSelf: 'center',
  },
});

AppRegistry.registerComponent('Nina', () => Nina);