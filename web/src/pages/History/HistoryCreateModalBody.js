import React, { useState } from 'react';
import { InputGroup, FormControl, Form } from 'react-bootstrap';
import axios from 'axios';
import StyleSelectButton from './StyleSelectButton';

export default function HistoryCreateModalBody() {
  const [files, setFiles] = useState('');

  const onLoadFile = (e) => {
    const file = e.target.files;
    console.log(file);
    setFiles(file);
  };

  const [selectedImg, setSelectedImg] = useState();
  const [selectedStyle, setSelectedStyle] = useState();
  const [selectedDate, setSelectedDate] = useState();
  const [selectedSubject, setSelectedSubject] = useState();
  const [selectedText, setSelectedText] = useState();

  const imgHandleChange = (e) => {
    console.log(e.target.files);
    setSelectedImg(e.target.files[0]);
  };
  const styleHandleChange = (e) => {
    console.log(e.target.files);
    setSelectedImg(e.target.files[0]);
  };
  const dateHandleChange = (e) => {
    setSelectedDate(e.target.value);
    // console.log(e.target.value);
  };
  const subjectHandleChange = (e) => {
    setSelectedSubject(e.target.value);
    // console.log(e.target.value);
  };
  const textHandleChange = (e) => {
    setSelectedText(e.target.value);
    // console.log(e.target.value);
  };

  const getMyStylesData = () => {
    axios
      .get('http://i6d104.p.ssafy.io:9999/api/style/finduserstyle', {
        headers: {
          'X-AUTH-TOKEN': `eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sInVzZXJQayI6IjEiLCJpYXQiOjE2NDM4Nzg4OTMsImV4cCI6MTY0NjQ3MDg5M30.Q2T5EQ38F53h1x037StKPwE-DBeqU0hBEAPY3D9w6WY`,
        },
      })
      .then((response) => {
        // console.log(response.data.data);
        // setSelectedStyle(response.data.data.url);
        console.log(response.data.data);
        console.log('성공');
        setSelectedStyle(response.data.data);
      });
  };

  const createHistories = () => {
    const formData = new FormData();
    formData.append('photos', selectedImg);
    formData.append('styleUrl', selectedStyle);
    formData.append('date', selectedDate);
    formData.append('subject', selectedSubject);
    formData.append('text', selectedText);

    axios
      .post('http://i6d104.p.ssafy.io:9999/api/history/add', formData, {
        headers: {
          'X-AUTH-TOKEN':
            'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwicm9sZXMiOlsiUk9MRV9VU0VSIl0sInVzZXJQayI6IjEiLCJpYXQiOjE2NDM4Nzg4OTMsImV4cCI6MTY0NjQ3MDg5M30.Q2T5EQ38F53h1x037StKPwE-DBeqU0hBEAPY3D9w6WY',
        },
      })
      .then((response) => {
        console.log(response);
      })
      .catch((err) => {
        console.log('실패');
      });
  };

  const getImg = (data) => {
    console.log(data);
    setSelectedStyle(data);
  };

  return (
    <form>
      <div className='row'>
        <div className='col-md-5'>
          <Form.Group controlId='dob'>
            <Form.Label>Select Date</Form.Label>
            <Form.Control type='date' name='dob' placeholder='Date of Birth' />
          </Form.Group>
        </div>
      </div>
      <Form.Label>Title</Form.Label>
      <InputGroup className='mb-3'>
        <FormControl />
      </InputGroup>

      <InputGroup>
        <InputGroup.Text>Comment</InputGroup.Text>
        <FormControl as='textarea' aria-label='With textarea' />
      </InputGroup>
      <hr />
      <h2>스타일가져오기</h2>
      {selectedStyle && (
        <img
          alt={selectedStyle.styleId}
          src={selectedStyle.url}
          style={{ margin: 'auto', maxWidth: '200px', maxHeight: '200px' }}
        />
      )}
      <div>
        <StyleSelectButton getImg={getImg} />
      </div>

      <hr />

      <h1>이미지 미리보기</h1>
      <table>
        <tbody>
          <tr>
            <td>
              <div>
                {selectedImg && (
                  <img
                    alt='sample'
                    src={URL.createObjectURL(selectedImg)}
                    style={{
                      margin: 'auto',
                      padding: 'auto',
                      width: '200px',
                      height: '200px',
                    }}
                  />
                )}
                <div
                  style={{
                    alignItems: 'center',
                    justifyContent: 'center',
                    display: 'flex',
                  }}
                >
                  <div>
                    <h4>사진 등록</h4>
                  </div>
                  <input
                    name='imgUpload'
                    type='file'
                    accept='image/*'
                    onChange={imgHandleChange}
                  />
                  {/* <button 
              style={{ backgroundColor: "gray", color: "white", width: "55px", height: "40px", cursor: "pointer", }} 
              onClick={() => deleteFileImage()} > 삭제 </button>  */}
                </div>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </form>
  );
}